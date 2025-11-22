package academy.io;

import academy.dto.Batch;
import academy.dto.LogEntry;
import academy.parse.LineParser;
import lombok.AllArgsConstructor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@AllArgsConstructor
public class RemoteBatchReader implements BatchReader {
    private final LineParser parser;
    private final URI uri;
    private final Predicate<Instant> timeFilter;
    private final int batchSize;

    @Override
    public void readBatches(Consumer<Batch> batchConsumer) throws IOException {
        List<LogEntry> buffer = new ArrayList<>(batchSize);
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int statusCode = response.statusCode();
            if (statusCode == 404) {
                throw new IOException("Remote resource not found: " + uri);
            }
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("Unexpected HTTP status " + statusCode + " for " + uri);
            }

            try (InputStream body = response.body();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null){
                    parser.parseLine(line)
                        .filter(entry -> timeFilter.test(entry.timestamp()))
                        .ifPresent(entry -> processEntry(entry, buffer, batchConsumer, batchSize));

                }
            }
            flush(buffer, batchConsumer);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread interrupted while reading remote resource: " + uri, e);
        }

    }
}
