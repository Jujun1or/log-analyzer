package academy.core;

import academy.dto.Batch;
import academy.io.BatchReader;
import academy.stats.BatchStats;
import academy.stats.GlobalStatsAggregator;
import academy.stats.ReportTotalStats;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class Pipeline {
    private final PipelineConfig config;
    private BlockingQueue<Batch> queue;
    private final List<BatchReader> readers = new ArrayList<>();

    private static final Batch POISON_PILL = new Batch(null);

    public Pipeline(PipelineConfig config) {
        this.config = config;
    }

    public ReportTotalStats run() throws IOException, RuntimeException {
        queue = new ArrayBlockingQueue<>(config.queueCapacity());

        GlobalStatsAggregator aggregator = new GlobalStatsAggregator();
        BatchProcessor processor = new BatchProcessor();

        ExecutorService consumerExecutor = Executors.newFixedThreadPool(config.numConsumers());
        List<Future<?>> consumerFutures = new ArrayList<>();

        for (int i = 0; i < config.numConsumers(); i++) {
            Future<?> future = consumerExecutor.submit(() -> consumerTask(processor, aggregator));
            consumerFutures.add(future);
        }

        ExecutorService producerExecutor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Void>> futures = new ArrayList<>();

        Consumer<Batch> enqueue = batch -> {
            try {
                queue.put(batch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while enqueueing batch: " + e);
            }
        };

        for (BatchReader reader : readers) {
            Callable<Void> task = () -> {
                reader.readBatches(enqueue);
                return null;
            };

            futures.add(producerExecutor.submit(task));
        }

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

            producerExecutor.shutdownNow();
            consumerExecutor.shutdownNow();

            if (cause instanceof IOException) {
                throw new IOException("Log file reading failed: " + cause);
            }

            throw new RuntimeException("Producer failed: " + cause);
        }

        sendPoisonPills(config.numConsumers());

        for (var future : consumerFutures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Processing failed: " + e);
            }
        }

        producerExecutor.shutdown();
        consumerExecutor.shutdown();

        return aggregator.buildReport();
    }

    public void registerReader(BatchReader reader) {
        readers.add(reader);
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
