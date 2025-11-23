package academy.report;

import academy.dto.ResolvedSource;
import academy.stats.ReportTotalStats;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JsonFormatter implements ReportFormatter {

    @Override
    public String format(ReportTotalStats stats, List<ResolvedSource> sources) {
        JsonReport dto = JsonReport.from(stats, sources);
        try {
            return JsonUtil.prettyWrite(dto);
        } catch (Exception e) {
            throw new RuntimeException("JSON formatting failed", e);
        }
    }

    public record JsonReport(
            List<String> files,
            @JsonProperty("totalRequestsCount") long totalRequests,
            @JsonProperty("responseSizeInBytes") ResponseSize responseSize,
            List<ResourceItem> resources,
            @JsonProperty("responseCodes") List<CodeItem> responseCodes,
            @JsonProperty("requestsPerDate") List<DateItem> requestsPerDate,
            @JsonProperty("uniqueProtocols") List<String> uniqueProtocols) {

        public static JsonReport from(ReportTotalStats stats, List<ResolvedSource> sources) {

            return new JsonReport(
                    sources.stream().map(ResolvedSource::displayName).toList(),
                    stats.totalRequests(),
                    new ResponseSize((long) stats.averageResponseSize(), stats.maxResponseSize(), stats.percentile95()),
                    stats.mostPopular10Resources().entrySet().stream()
                            .map(e -> new ResourceItem(e.getKey(), e.getValue()))
                            .toList(),
                    stats.responseCodes().entrySet().stream()
                            .map(e -> new CodeItem(e.getKey(), e.getValue()))
                            .toList(),
                    stats.requestsByDateInPercents().values().stream()
                            .map(info -> new DateItem(
                                    info.date(),
                                    info.weekday(),
                                    info.totalRequestsCount(),
                                    info.totalRequestsPercentage()))
                            .toList(),
                    stats.uniqueProtocols().stream().toList());
        }
    }

    public record ResponseSize(long average, long max, long p95) {}

    public record ResourceItem(String resource, long totalRequestsCount) {}

    public record CodeItem(int code, long totalResponsesCount) {}

    public record DateItem(String date, String weekday, long totalRequestsCount, double totalRequestsPercentage) {}
}
