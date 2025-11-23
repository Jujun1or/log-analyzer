package academy.io;

import academy.dto.Batch;
import academy.dto.LogEntry;
import academy.parse.LineParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LocalBatchReader implements BatchReader {
    private final LineParser parser;
    private final Path path;
    private final Predicate<Instant> timeFilter;
    private final int batchSize;

    public LocalBatchReader(LineParser parser, Path path, Predicate<Instant> timeFilter, int batchSize) {
        this.parser = parser;
        this.path = path;
        this.timeFilter = timeFilter;
        this.batchSize = batchSize;
    }

    @Override
    public void readBatches(Consumer<Batch> batchConsumer) throws IOException {
        List<LogEntry> buffer = new ArrayList<>(batchSize);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                parser.parseLine(line)
                    .filter(entry -> timeFilter.test(entry.timestamp()))
                    .ifPresent(entry -> processEntry(entry, buffer, batchConsumer, batchSize));
            }
        }

        flush(buffer, batchConsumer);
    }
}
