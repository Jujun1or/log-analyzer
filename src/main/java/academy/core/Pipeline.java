package academy.core;

import academy.dto.Batch;
import academy.io.BatchReader;
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

    public Pipeline(PipelineConfig config) {
        this.config = config;
    }

    public void run() throws IOException, RuntimeException {
        queue = new ArrayBlockingQueue<>(config.queueCapacity());
        ExecutorService producerExecutor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Void>> futures = new ArrayList<>();

        Consumer<Batch> enqueue = batch -> {
            try {
                queue.put(batch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while enqueueing batch", e);
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
            Thread.currentThread().interrupt(); // восстановить флаг
            throw new IOException("Interrupted while waiting for producers", e);

        } catch (ExecutionException e) {

            Throwable cause = e.getCause();

            if (cause instanceof IOException) {
                throw new IOException("Log file reading failed: ", cause);
            }

            throw new RuntimeException("Producer failed", cause);
        }

    }

    public void registerReader(BatchReader reader) {
        readers.add(reader);
    }
}
