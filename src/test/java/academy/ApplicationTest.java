package academy;

import static org.junit.jupiter.api.Assertions.*;

import academy.core.LogProcessingOrchestrator;
import academy.dto.Arguments;
import academy.enums.ReportFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ApplicationTest {

    @Test
    @DisplayName("Базовая проверка работоспособности программы")
    void happyPathTest() throws Exception {

        Path log = Files.createTempFile("test-log", ".log");
        Files.writeString(
                log,
                String.join(
                        "\n",
                        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /a HTTP/1.1\" 200 100",
                        "93.180.71.3 - - [17/May/2015:08:05:33 +0000] \"GET /b HTTP/1.1\" 404 300"));

        Path out = log.getParent().resolve("report.json");
        Files.deleteIfExists(out);

        Arguments args = new Arguments(List.of(log.toString()), ReportFormat.JSON, out, null, null);

        int exit = LogProcessingOrchestrator.run(args);

        assertEquals(0, exit, "Ожидался код выхода 0");

        assertTrue(Files.exists(out), "Выходной файл отчёта должен существовать");

        String json = Files.readString(out);
        assertTrue(json.contains("\"totalRequestsCount\""), "В отчёте должен быть totalRequestsCount");
        assertTrue(json.contains("\"responseCodes\""), "В отчёте должен быть responseCodes");
        assertTrue(json.contains("\"resources\""), "В отчёте должен быть resources");
    }
}
