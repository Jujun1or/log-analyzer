package academy.acceptance;

import academy.dto.Batch;
import academy.dto.LogEntry;
import academy.io.LocalBatchReader;
import academy.io.RemoteBatchReader;
import academy.parse.LineParser;
import academy.parse.NginxLineParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class LogFileParsingTest {

    private static final String VALID_LINE1 =
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] " +
            "\"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"UA\"";

    private static final String VALID_LINE2 =
        "93.180.71.3 - - [18/May/2015:10:15:00 +0000] " +
            "\"GET /downloads/product_2 HTTP/1.1\" 200 100 \"-\" \"UA\"";

    private static final String BROKEN_LINE = "this is not nginx";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest() throws Exception {
        Path file = tempDir.resolve("local.log");
        Files.writeString(file, VALID_LINE1 + "\n" + VALID_LINE2);

        LineParser parser = new NginxLineParser();
        Predicate<Instant> allowAll = i -> true;

        LocalBatchReader reader =
            new LocalBatchReader(parser, file, allowAll, 50);

        List<Batch> batches = new ArrayList<>();
        reader.readBatches(batches::add);

        List<LogEntry> entries = batches.stream()
            .flatMap(b -> b.entries().stream())
            .toList();

        assertEquals(2, entries.size());
    }


    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/log", exchange -> {
            String content = VALID_LINE1 + "\n" + VALID_LINE2;
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        int port = server.getAddress().getPort();
        URI uri = new URI("http://localhost:" + port + "/log");

        LineParser parser = new NginxLineParser();
        Predicate<Instant> allowAll = i -> true;

        RemoteBatchReader reader =
            new RemoteBatchReader(parser, uri, allowAll, 50);

        List<Batch> batches = new ArrayList<>();
        reader.readBatches(batches::add);

        List<LogEntry> entries = batches.stream()
            .flatMap(b -> b.entries().stream())
            .toList();

        server.stop(0);

        assertEquals(2, entries.size());
    }


    @Test
    @DisplayName("На вход передан валидный локальный log-файл, часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest() throws Exception {
        Path file = tempDir.resolve("filtered.log");
        Files.writeString(file, VALID_LINE1 + "\n" + VALID_LINE2);

        LineParser parser = new NginxLineParser();

        Instant from = Instant.parse("2015-05-18T00:00:00Z");
        Instant to   = Instant.parse("2015-05-19T00:00:00Z");
        Predicate<Instant> filter =
            ts -> !ts.isBefore(from) && ts.isBefore(to);

        LocalBatchReader reader =
            new LocalBatchReader(parser, file, filter, 50);

        List<Batch> batches = new ArrayList<>();
        reader.readBatches(batches::add);

        List<LogEntry> entries = batches.stream()
            .flatMap(b -> b.entries().stream())
            .toList();

        assertEquals(1, entries.size());
        assertEquals("/downloads/product_2", entries.get(0).resource());
    }


    // ============================================================
    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest() throws Exception {
        Path file = tempDir.resolve("broken.log");
        Files.writeString(file, VALID_LINE1 + "\n" + BROKEN_LINE + "\n" + VALID_LINE2);

        LineParser parser = new NginxLineParser();
        Predicate<Instant> allowAll = i -> true;

        LocalBatchReader reader =
            new LocalBatchReader(parser, file, allowAll, 50);

        List<Batch> batches = new ArrayList<>();
        reader.readBatches(batches::add);

        List<LogEntry> entries = batches.stream()
            .flatMap(b -> b.entries().stream())
            .toList();

        assertEquals(2, entries.size());
    }
}
