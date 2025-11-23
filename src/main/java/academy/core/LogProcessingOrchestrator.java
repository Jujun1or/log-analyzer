package academy.core;

import academy.dto.Arguments;
import academy.dto.ResolvedSource;
import academy.io.BatchReader;
import academy.io.LocalBatchReader;
import academy.io.LogSourceResolver;
import academy.io.RemoteBatchReader;
import academy.parse.LineParser;
import academy.parse.NginxLineParser;
import academy.stats.ReportTotalStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import static java.lang.System.exit;

public class LogProcessingOrchestrator {
    private static final Logger log = LogManager.getLogger(LogProcessingOrchestrator.class);

    public static void run(Arguments arguments){
        List<ResolvedSource> resolvedSources = LogSourceResolver.resolve(arguments.paths());
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

        PipelineConfig config = new PipelineConfig(100, 16,
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
            reportTotalStats = pipeline.run();
            System.out.println(reportTotalStats);
        } catch (IOException e) {
            log.error(e.getMessage());
            exit(2);
            return;
        } catch (Exception e) {
            log.error(e.getMessage());
            exit(1);
            return;
        }

        exit(0);
    }
}
