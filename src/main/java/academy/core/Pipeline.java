package academy.core;

import academy.dto.Batch;
import academy.io.BatchReader;
import academy.stats.BatchStats;
import academy.stats.GlobalStatsAggregator;
import academy.stats.ReportTotalStats;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Pipeline {
    private final PipelineConfig config;
    private BlockingQueue<Batch> queue;
    private final List<BatchReader> readers = new ArrayList<>();

    private static final Batch POISON_PILL = new Batch(null);

    public Pipeline(PipelineConfig config) {
        this.config = config;
    }

    public ReportTotalStats run() throws IOException {
        queue = new ArrayBlockingQueue<>(config.queueCapacity());

        GlobalStatsAggregator aggregator = new GlobalStatsAggregator();
        BatchProcessor processor = new BatchProcessor();

        ExecutorService consumerExecutor = Executors.newFixedThreadPool(config.numConsumers());
        List<Future<?>> consumerFutures = startConsumers(consumerExecutor, processor, aggregator);

        ExecutorService producerExecutor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Void>> producerFutures = startProducers(producerExecutor);

        waitForProducers(producerFutures, consumerExecutor, producerExecutor);

        sendPoisonPills(config.numConsumers());
        waitForConsumers(consumerFutures);

        shutdownExecutors(producerExecutor, consumerExecutor);

        return aggregator.buildReport();
    }

    public void registerReader(BatchReader reader) {
        readers.add(reader);
    }

    private List<Future<?>> startConsumers(
            ExecutorService executor, BatchProcessor processor, GlobalStatsAggregator aggregator) {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < config.numConsumers(); i++) {
            futures.add(executor.submit(() -> consumerTask(processor, aggregator)));
        }
        return futures;
    }

    private List<Future<Void>> startProducers(ExecutorService executor) {
        List<Future<Void>> futures = new ArrayList<>();
        Consumer<Batch> enqueue = createEnqueueConsumer();

        for (BatchReader reader : readers) {
            Callable<Void> task = () -> {
                reader.readBatches(enqueue);
                return null;
            };
            futures.add(executor.submit(task));
        }
        return futures;
    }

    private Consumer<Batch> createEnqueueConsumer() {
        return batch -> {
            try {
                queue.put(batch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while enqueueing batch: " + e);
            }
        };
    }

    private void waitForProducers(
            List<Future<Void>> futures, ExecutorService consumerExecutor, ExecutorService producerExecutor)
            throws IOException {
        try {
            for (Future<Void> f : futures) {
                f.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for producers: " + e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            sendPoisonPills(config.numConsumers());
            shutdownExecutors(producerExecutor, consumerExecutor);

            if (cause instanceof IOException) {
                throw new IOException("Log file reading failed: " + cause);
            }
            throw new RuntimeException("Producer failed: " + cause);
        }
    }

    private void waitForConsumers(List<Future<?>> consumerFutures) {
        for (Future<?> f : consumerFutures) {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException("Processing failed: " + e);
            }
        }
    }

    private void shutdownExecutors(ExecutorService producers, ExecutorService consumers) {
        producers.shutdown();
        consumers.shutdown();
    }

    private void sendPoisonPills(int count) {
        for (int i = 0; i < count; i++) {
            try {
                queue.put(POISON_PILL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while sending poison pill: " + e);
            }
        }
    }

    private void consumerTask(BatchProcessor processor, GlobalStatsAggregator aggregator) {
        try {
            while (true) {
                Batch batch = queue.take();
                if (batch == POISON_PILL) break;

                BatchStats stats = processor.process(batch);
                aggregator.merge(stats);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted: " + e);
        }
    }
}
