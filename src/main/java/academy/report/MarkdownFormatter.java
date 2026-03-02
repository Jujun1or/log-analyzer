package academy.report;

import academy.dto.ResolvedSource;
import academy.stats.ReportTotalStats;
import java.util.List;
import java.util.stream.Collectors;

public class MarkdownFormatter implements ReportFormatter {

    @Override
    public String format(ReportTotalStats stats, List<ResolvedSource> sources) {

        StringBuilder sb = new StringBuilder();

        sb.append("#### Общая информация\n\n");

        sb.append("| Метрика | Значение |\n");
        sb.append("|--------|----------|\n");
        sb.append("| Файл(-ы) | `")
                .append(sources.stream().map(ResolvedSource::displayName).collect(Collectors.joining(", ")))
                .append("` |\n");
        sb.append("| Количество запросов | ").append(stats.totalRequests()).append(" |\n");
        sb.append("| Средний размер ответа | ")
                .append(stats.averageResponseSize())
                .append("b |\n");
        sb.append("| Максимальный размер ответа | ")
                .append(stats.maxResponseSize())
                .append("b |\n");
        sb.append("| 95p размера ответа | ").append(stats.percentile95()).append("b |\n\n");

        sb.append("#### Запрашиваемые ресурсы\n\n");
        sb.append("| Ресурс | Количество |\n");
        sb.append("|--------|-----------|\n");
        stats.mostPopular10Resources()
                .forEach((res, cnt) ->
                        sb.append("| ").append(res).append(" | ").append(cnt).append(" |\n"));

        sb.append("\n#### Коды ответа\n\n");
        sb.append("| Код | Количество |\n");
        sb.append("|-----|-----------|\n");
        stats.responseCodes()
                .forEach((code, cnt) ->
                        sb.append("| ").append(code).append(" | ").append(cnt).append(" |\n"));

        sb.append("\n#### Распределение запросов по датам\n\n");
        sb.append("| Дата | День недели | Количество | Процент |\n");
        sb.append("|------|--------------|-----------|---------|\n");
        stats.requestsByDateInPercents().values().forEach(info -> {
            sb.append("| ")
                    .append(info.date())
                    .append(" | ")
                    .append(info.weekday())
                    .append(" | ")
                    .append(info.totalRequestsCount())
                    .append(" | ")
                    .append(String.format("%.2f", info.totalRequestsPercentage()))
                    .append("% |\n");
        });

        sb.append("\n#### Уникальные протоколы\n\n");
        sb.append(stats.uniqueProtocols().stream().collect(Collectors.joining(", ", "`", "`")));

        return sb.toString();
    }
}
