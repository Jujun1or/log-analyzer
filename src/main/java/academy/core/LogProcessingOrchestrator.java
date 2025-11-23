package academy.core;

import academy.dto.Arguments;
import academy.dto.ResolvedSource;
import academy.io.BatchReader;
import academy.io.LocalBatchReader;
import academy.io.LogSourceResolver;
import academy.io.RemoteBatchReader;
import academy.parse.LineParser;
import academy.parse.NginxLineParser;
import academy.report.ReportFormatter;
import academy.report.ReportFormatterFactory;
import academy.stats.ReportTotalStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

public class LogProcessingOrchestrator {
    private static final Logger log = LogManager.getLogger(LogProcessingOrchestrator.class);

    public static int run(Arguments arguments){
        log.info("Starting log analysis");
        List<ResolvedSource> resolvedSources = LogSourceResolver.resolve(arguments.paths());
        log.info("Total resolved sources: {}", resolvedSources.size());
        Instant from = arguments.from();
        Instant to = arguments.to();

        Predicate<Instant> timeFilter = instant -> {
            if (instant == null) return false;

            if (from != null && instant.isBefore(from)) {
                return false;
            }

            if (to != null && instant.isAfter(to)) {
                return false;
            }

            return true;
        };

        PipelineConfig config = new PipelineConfig(1000, 16,
            Runtime.getRuntime().availableProcessors());

        Pipeline pipeline = new Pipeline(config);

        LineParser parser = new NginxLineParser();
        for (var source : resolvedSources){
            BatchReader reader = source.remote()
                ? new RemoteBatchReader(parser, source.uri(), timeFilter, config.batchSize())
                : new LocalBatchReader(parser, source.path(), timeFilter, config.batchSize());

            pipeline.registerReader(reader);
        }

        ReportTotalStats reportTotalStats;

        try {
            log.info("Running pipeline...");
            reportTotalStats = pipeline.run();
            log.info("Pipeline finished. Generating report...");
            ReportFormatter reportFormatter = ReportFormatterFactory.create(arguments.reportFormat());

            String content = reportFormatter.format(reportTotalStats, resolvedSources);
            Files.writeString(arguments.output(), content);

        } catch (IOException e) {
            log.error(e.getMessage());
            return 2;
        } catch (Exception e) {
            log.error(e.getMessage());
            return 1;
        }

        log.info("Report successfully generated!");
        return 0;
    }
}
