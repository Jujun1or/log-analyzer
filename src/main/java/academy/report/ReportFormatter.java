package academy.report;

import academy.dto.ResolvedSource;
import academy.stats.ReportTotalStats;
import java.util.List;

public interface ReportFormatter {
    String format(ReportTotalStats stats, List<ResolvedSource> sources);
}
