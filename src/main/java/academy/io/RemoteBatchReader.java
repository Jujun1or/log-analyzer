package academy.io;

import academy.dto.Batch;
import academy.dto.LogEntry;
import academy.parse.LineParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RemoteBatchReader implements BatchReader {

    private static final HttpClient CLIENT =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final LineParser parser;
    private final URI uri;
    private final Predicate<Instant> timeFilter;
    private final int batchSize;

    public RemoteBatchReader(LineParser parser, URI uri, Predicate<Instant> timeFilter, int batchSize) {
        this.parser = parser;
        this.uri = uri;
        this.timeFilter = timeFilter;
        this.batchSize = batchSize;
    }

    @Override
    public void readBatches(Consumer<Batch> batchConsumer) throws IOException {

        List<LogEntry> buffer = new ArrayList<>(batchSize);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();

            if (status >= 400 && status < 500) {
                throw new IOException("Client error (4xx) when fetching " + uri);
            }
            if (status < 200 || status >= 300) {
                throw new IOException("Unexpected HTTP status " + status + " for " + uri);
            }

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    parser.parseLine(line)
                            .filter(entry -> timeFilter.test(entry.timestamp()))
                            .ifPresent(entry -> processEntry(entry, buffer, batchConsumer, batchSize));
                }
            }

            flush(buffer, batchConsumer);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading remote resource " + uri, e);
        }
    }
}
