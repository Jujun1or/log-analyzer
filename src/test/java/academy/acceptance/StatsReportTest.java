package academy.acceptance;

import academy.dto.ResolvedSource;
import academy.report.JsonFormatter;
import academy.report.MarkdownFormatter;
import academy.report.AdocFormatter;
import academy.stats.ReportTotalStats;
import academy.stats.RequestDateInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatsReportTest {

    private ReportTotalStats sampleStats() {
        return new ReportTotalStats(
            3,
            150.0,
            300,
            300,
            Map.of(200, 1L, 404, 2L),
            Map.of("/a", 2L, "/b", 1L),
            Map.of(
                "2025-01-01",
                new RequestDateInfo("2025-01-01", "WEDNESDAY", 3, 100.0)
            ),
            Set.of("HTTP/1.1")
        );
    }

    private List<ResolvedSource> sampleFiles() {
        return List.of(
            ResolvedSource.createLocal(java.nio.file.Path.of("access.log"))
        );
    }


    @Test
    @DisplayName("Сохранение статистики в формате JSON — полный тест")
    void jsonTest() {
        JsonFormatter formatter = new JsonFormatter();

        ReportTotalStats stats = sampleStats();
        List<ResolvedSource> files = sampleFiles();

        String json = formatter.format(stats, files);

        assertTrue(json.trim().startsWith("{"));
        assertTrue(json.trim().endsWith("}"));

        assertTrue(json.contains("\"files\""));
        assertTrue(json.contains("\"totalRequestsCount\""));
        assertTrue(json.contains("\"responseSizeInBytes\""));
        assertTrue(json.contains("\"resources\""));
        assertTrue(json.contains("\"responseCodes\""));
        assertTrue(json.contains("\"requestsPerDate\""));
        assertTrue(json.contains("\"uniqueProtocols\""));

        assertTrue(json.contains("\"files\" : ["));
        assertTrue(json.contains("\"access.log\""));

        assertTrue(json.contains("\"totalRequestsCount\" : 3"));
        assertTrue(json.contains("\"average\" : 150"));
        assertTrue(json.contains("\"max\" : 300"));
        assertTrue(json.contains("\"p95\" : 300"));

        assertTrue(json.contains("\"resources\" : ["));
        assertTrue(json.contains("\"resource\" : \"/a\""));
        assertTrue(json.contains("\"totalRequestsCount\" : 2"));
        assertTrue(json.contains("\"resource\" : \"/b\""));
        assertTrue(json.contains("\"totalRequestsCount\" : 1"));

        assertTrue(json.contains("\"responseCodes\" : ["));
        assertTrue(json.contains("\"code\" : 200"));
        assertTrue(json.contains("\"totalResponsesCount\" : 1"));
        assertTrue(json.contains("\"code\" : 404"));
        assertTrue(json.contains("\"totalResponsesCount\" : 2"));

        assertTrue(json.contains("\"requestsPerDate\""));
        assertTrue(json.contains("\"date\" : \"2025-01-01\""));
        assertTrue(json.contains("\"weekday\" : \"WEDNESDAY\""));
        assertTrue(json.contains("\"totalRequestsCount\" : 3"));
        assertTrue(json.contains("\"totalRequestsPercentage\" : 100.0"));

        assertTrue(json.contains("\"uniqueProtocols\" : ["));
        assertTrue(json.contains("\"HTTP/1.1\""));
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest() {
        MarkdownFormatter f = new MarkdownFormatter();
        String out = f.format(sampleStats(), sampleFiles());

        assertTrue(out.contains("#### Общая информация"));
        assertTrue(out.contains("#### Запрашиваемые ресурсы"));
        assertTrue(out.contains("#### Коды ответа"));
        assertTrue(out.contains("#### Распределение запросов по датам"));
        assertTrue(out.contains("#### Уникальные протоколы"));

        assertTrue(out.contains("`access.log`"));
        assertTrue(out.contains("| Количество запросов | 3 |"));
        assertTrue(out.contains("| Средний размер ответа | 150.0b |"));
        assertTrue(out.contains("| 95p размера ответа | 300b |"));

        assertTrue(out.contains("| /a | 2 |"));
        assertTrue(out.contains("| /b | 1 |"));

        assertTrue(out.contains("| 200 | 1 |"));
        assertTrue(out.contains("| 404 | 2 |"));

        assertTrue(out.contains("`HTTP/1.1`"));
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest() {
        AdocFormatter f = new AdocFormatter();
        String out = f.format(sampleStats(), sampleFiles());

        assertTrue(out.contains("= Отчёт по логам NGINX"));
        assertTrue(out.contains("== Общая информация"));
        assertTrue(out.contains("== Топ-ресурсов"));
        assertTrue(out.contains("== Коды ответа"));
        assertTrue(out.contains("== Распределение запросов по датам"));
        assertTrue(out.contains("== Уникальные протоколы"));

        assertTrue(out.contains("|Файл(-ы) |access.log"));
        assertTrue(out.contains("|Количество запросов |3"));
        assertTrue(out.contains("|Средний размер ответа |150.0b"));
        assertTrue(out.contains("|Максимальный размер ответа |300b"));
        assertTrue(out.contains("|95-й перцентиль |300b"));

        assertTrue(out.contains("|/a | 2"));
        assertTrue(out.contains("|/b | 1"));

        assertTrue(out.contains("|200 | 1"));
        assertTrue(out.contains("|404 | 2"));

        assertTrue(out.contains("HTTP/1.1"));
    }
}
