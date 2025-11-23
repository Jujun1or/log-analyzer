package academy.report;

import academy.dto.ResolvedSource;
import academy.stats.ReportTotalStats;

import java.util.List;
import java.util.stream.Collectors;

public class AdocFormatter implements ReportFormatter {

    @Override
    public String format(ReportTotalStats stats, List<ResolvedSource> sources) {

        StringBuilder sb = new StringBuilder();

        sb.append("= Отчёт по логам NGINX\n\n");

        sb.append("== Общая информация\n\n");
        sb.append("|===\n");
        sb.append("|Метрика |Значение\n");

        sb.append("|Файл(-ы) |")
            .append(sources.stream().map(ResolvedSource::displayName).collect(Collectors.joining(", ")))
            .append("\n");
        sb.append("|Количество запросов |").append(stats.totalRequests()).append("\n");
        sb.append("|Средний размер ответа |").append(stats.averageResponseSize()).append("b\n");
        sb.append("|Максимальный размер ответа |").append(stats.maxResponseSize()).append("b\n");
        sb.append("|95-й перцентиль |").append(stats.percentile95()).append("b\n");
        sb.append("|===\n\n");

        sb.append("== Топ-ресурсов\n\n");
        sb.append("|===\n|Ресурс |Количество\n");
        stats.mostPopular10Resources().forEach(
            (r, c) -> sb.append("|").append(r).append(" | ").append(c).append("\n")
        );
        sb.append("|===\n\n");

        sb.append("== Коды ответа\n\n");
        sb.append("|===\n|Код |Количество\n");
        stats.responseCodes().forEach(
            (code, cnt) -> sb.append("|").append(code).append(" | ").append(cnt).append("\n")
        );
        sb.append("|===\n\n");

        sb.append("== Распределение запросов по датам\n\n");
        sb.append("|===\n|Дата |День недели |Количество |Процент\n");
        stats.requestsByDateInPercents().values().forEach(info -> {
            sb.append("|").append(info.date()).append(" | ")
                .append(info.weekday()).append(" | ")
                .append(info.totalRequestsCount()).append(" | ")
                .append(String.format("%.2f", info.totalRequestsPercentage())).append("%\n");
        });
        sb.append("|===\n\n");

        sb.append("== Уникальные протоколы\n\n");
        sb.append(String.join(", ", stats.uniqueProtocols()));

        return sb.toString();
    }
}
