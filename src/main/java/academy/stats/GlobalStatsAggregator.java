package academy.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class GlobalStatsAggregator {

    private long totalRequests = 0;
    private long totalBytes = 0;
    private int maxBytes = 0;

    private final Map<Integer, Long> responseSizeFreq = new HashMap<>();
    private final Map<Integer, Long> responseCodes = new HashMap<>();
    private final Map<String, Long> resources = new HashMap<>();
    private final Map<LocalDate, Long> requestsByDate = new HashMap<>();
    private final Set<String> uniqueProtocols = new HashSet<>();

    public synchronized void merge(BatchStats stats) {
        totalRequests += stats.totalRequests();
        totalBytes += stats.totalBytes();
        maxBytes = Math.max(maxBytes, stats.maxBytes());

        mergeMap(responseSizeFreq, stats.responseSizeFreq());
        mergeMap(responseCodes, stats.responseCade());
        mergeMap(resources, stats.resources());
        mergeMap(requestsByDate, stats.requestsByDate());

        uniqueProtocols.addAll(stats.uniqueProtocols());
    }

    private <K> void mergeMap(Map<K, Long> target, Map<K, Long> src) {
        for (var e : src.entrySet()) {
            target.merge(e.getKey(), e.getValue(), Long::sum);
        }
    }

    public synchronized ReportTotalStats buildReport() {

        Map<String, Long> top10Resources = get10MostPopularResources();
        double p95 = find95Percentile();
        double avg = totalRequests == 0 ? 0 : (double) totalBytes / totalRequests;
        Map<String, RequestDateInfo> dateDistribution = findRequestsByDateInPercents();

        return new ReportTotalStats(
                totalRequests,
                round2(avg),
                maxBytes,
                p95,
                responseCodes,
                top10Resources,
                dateDistribution,
                uniqueProtocols);
    }

    private Map<String, Long> get10MostPopularResources() {
        return resources.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private double find95Percentile() {
        if (totalRequests == 0) {
            return 0;
        }

        double p = 0.95;
        long n = totalRequests;

        double pos = 1 + (n - 1) * p;
        long lowerRank = (long) Math.floor(pos);
        long upperRank = (long) Math.ceil(pos);

        List<Map.Entry<Integer, Long>> sorted = responseSizeFreq.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        long cumulative = 0;
        Integer lowerVal = null;
        Integer upperVal = null;

        for (var entry : sorted) {
            cumulative += entry.getValue();

            if (lowerVal == null && cumulative >= lowerRank) {
                lowerVal = entry.getKey();
            }
            if (cumulative >= upperRank) {
                upperVal = entry.getKey();
                break;
            }
        }

        if (lowerVal == null || upperVal == null) {
            return maxBytes;
        }

        if (lowerRank == upperRank || lowerVal.equals(upperVal)) {
            return lowerVal;
        }

        double fraction = (pos - lowerRank) / (upperRank - lowerRank);
        double interpolated = round2(lowerVal + fraction * (upperVal - lowerVal));

        return interpolated;
    }

    private Map<String, RequestDateInfo> findRequestsByDateInPercents() {
        Map<String, RequestDateInfo> result = new LinkedHashMap<>();

        if (totalRequests == 0) return result;

        for (var entry : requestsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            long count = entry.getValue();
            double percent = round2(count * 100.0 / totalRequests);

            result.put(
                    date.toString(),
                    new RequestDateInfo(
                            date.toString(),
                            date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                            count,
                            percent));
        }

        return result;
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
