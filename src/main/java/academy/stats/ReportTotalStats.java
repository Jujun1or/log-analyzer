package academy.stats;

import java.util.Map;
import java.util.Set;

public record ReportTotalStats(
    long totalRequests,
    double averageResponseSize,
    int maxResponseSize,
    int percentile95,
    Map<Integer, Long> responseCodes,
    Map<String, Long> mostPopular10Resources,
    Map<String, RequestDateInfo> requestsByDateInPercents,
    Set<String> uniqueProtocols
) {}
