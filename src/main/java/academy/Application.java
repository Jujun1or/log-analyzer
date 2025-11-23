package academy;

import academy.cli.ArgumentsValidator;
import academy.core.LogProcessingOrchestrator;
import academy.dto.Arguments;
import academy.enums.ReportFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Command(name = "Log Analyzer", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    private static final Logger log = LogManager.getLogger(LogProcessingOrchestrator.class);

    @CommandLine.Option(names = {"--path", "-p"}, required = true, arity = "1..*")
    private List<String> paths;

    @CommandLine.Option(names = {"--format", "-f"})
    private String formatRaw;

    @CommandLine.Option(names = {"--output", "-o"}, required = true)
    private Path outputFile;

    @CommandLine.Option(names = "--from")
    private String fromRaw;

    @CommandLine.Option(names = "--to")
    private String toRaw;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            ArgumentsValidator.validate(paths, formatRaw, outputFile, fromRaw, toRaw);

            ReportFormat reportFormat = ReportFormat.valueOf(formatRaw.toUpperCase());
            Instant from = ArgumentsValidator.parseInstantOrNull(fromRaw);
            Instant to = ArgumentsValidator.parseInstantOrNull(toRaw);

            Arguments args = new Arguments(paths, reportFormat, outputFile, from, to);
            LogProcessingOrchestrator.run(args);

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            System.exit(2);
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    }
}
