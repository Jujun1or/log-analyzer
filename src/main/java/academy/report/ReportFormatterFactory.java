package academy.report;

import academy.enums.ReportFormat;

public class ReportFormatterFactory {
    public static ReportFormatter create(ReportFormat reportFormat) {
        return switch (reportFormat) {
            case JSON -> new JsonFormatter();
            case MARKDOWN -> new MarkdownFormatter();
            case ADOC -> new AdocFormatter();
        };
    }
}
