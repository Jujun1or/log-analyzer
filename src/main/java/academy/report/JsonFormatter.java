package academy.report;

import academy.dto.ResolvedSource;
import academy.stats.ReportTotalStats;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
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
                    sources.stream()
                            .map(s -> {
                                if (s.remote()) {
                                    return s.displayName();
                                } else {
                                    return Path.of(s.displayName())
                                            .getFileName()
                                            .toString(); // имя файла
                                }
                            })
                            .sorted()
                            .toList(),
                    stats.totalRequests(),
                    new ResponseSize(stats.averageResponseSize(), stats.maxResponseSize(), stats.percentile95()),
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
                    stats.uniqueProtocols().stream().sorted().toList());
        }
    }

    public record ResponseSize(double average, double max, double p95) {}

    public record ResourceItem(String resource, long totalRequestsCount) {}

    public record CodeItem(int code, long totalResponsesCount) {}

    public record DateItem(String date, String weekday, long totalRequestsCount, double totalRequestsPercentage) {}
}
