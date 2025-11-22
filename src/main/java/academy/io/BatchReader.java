package academy.io;

import academy.dto.Batch;
import academy.dto.LogEntry;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface BatchReader {
    void readBatches(Consumer<Batch> batchConsumer) throws IOException;

    default void processEntry(LogEntry entry, List<LogEntry> buffer, Consumer<Batch> consumer, int batchSize) {
        buffer.add(entry);
        if (buffer.size() >= batchSize) {
            flush(buffer, consumer);
        }
    }

    default void flush(List<LogEntry> buffer, Consumer<Batch> consumer) {
        if (buffer.isEmpty()) return;

        consumer.accept(new Batch(List.copyOf(buffer)));
        buffer.clear();
    }
}
