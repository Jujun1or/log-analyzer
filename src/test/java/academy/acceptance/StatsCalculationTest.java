package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.core.BatchProcessor;
import academy.dto.Batch;
import academy.dto.LogEntry;
import academy.stats.BatchStats;
import academy.stats.GlobalStatsAggregator;
import academy.stats.ReportTotalStats;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StatsCalculationTest {

    @Test
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() {

        BatchProcessor processor = new BatchProcessor();
        GlobalStatsAggregator aggregator = new GlobalStatsAggregator();

        LogEntry e1 = new LogEntry(
                Instant.parse("2015-05-17T08:05:32Z"), "GET", "/downloads/product_1", "HTTP/1.1", 200, 100);

        LogEntry e2 = new LogEntry(
                Instant.parse("2015-05-17T10:00:00Z"), "GET", "/downloads/product_1", "HTTP/1.1", 404, 300);

        LogEntry e3 =
                new LogEntry(Instant.parse("2015-05-18T09:00:00Z"), "GET", "/downloads/product_2", "HTTP/1.1", 304, 50);

        Batch batch = new Batch(List.of(e1, e2, e3));

        BatchStats stats = processor.process(batch);
        aggregator.merge(stats);
        ReportTotalStats report = aggregator.buildReport();

        assertEquals(3, report.totalRequests());
        assertEquals(300, report.maxResponseSize());
        assertEquals(150, report.averageResponseSize());

        assertEquals(300, report.percentile95());

        assertEquals(2, report.mostPopular10Resources().size());
        assertEquals(2, report.mostPopular10Resources().get("/downloads/product_1"));
        assertEquals(1, report.mostPopular10Resources().get("/downloads/product_2"));

        assertEquals(3, report.responseCodes().size());
        assertEquals(1, report.responseCodes().get(200));
        assertEquals(1, report.responseCodes().get(404));
        assertEquals(1, report.responseCodes().get(304));

        assertEquals(1, report.uniqueProtocols().size());
        assertTrue(report.uniqueProtocols().contains("HTTP/1.1"));

        assertEquals(2, report.requestsByDateInPercents().size());
        assertTrue(report.requestsByDateInPercents().containsKey("2015-05-17"));
        assertTrue(report.requestsByDateInPercents().containsKey("2015-05-18"));
    }
}
