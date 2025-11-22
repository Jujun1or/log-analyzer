package academy.core;

import academy.dto.Arguments;
import academy.dto.ResolvedSource;
import academy.io.BatchReader;
import academy.io.LocalBatchReader;
import academy.io.LogSourceResolver;
import academy.io.RemoteBatchReader;
import academy.parse.LineParser;
import academy.parse.NginxLineParser;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

public class LogProcessingOrchestrator {
    public static int run(Arguments arguments){
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



        return 0;
    }
}
