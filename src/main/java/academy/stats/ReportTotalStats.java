package academy.stats;

import java.util.List;
import java.util.Map;

public record ReportTotalStats(
        long totalRequests,
        double averageResponseSize,
        double maxResponseSize,
        double percentile95,
        Map<Integer, Long> responseCodes,
        Map<String, Long> mostPopular10Resources,
        Map<String, RequestDateInfo> requestsByDateInPercents,
        List<String> uniqueProtocols) {}
