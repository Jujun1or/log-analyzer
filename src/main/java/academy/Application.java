package academy;

import static java.lang.System.exit;

import academy.cli.ArgumentsValidator;
import academy.core.LogProcessingOrchestrator;
import academy.dto.Arguments;
import academy.enums.ReportFormat;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "Log Analyzer", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    private static final Logger log = LogManager.getLogger(LogProcessingOrchestrator.class);

    @CommandLine.Option(
            names = {"--path", "-p"},
            required = true,
            arity = "1..*")
    private List<String> paths;

    @CommandLine.Option(names = {"--format", "-f"})
    private String formatRaw;

    @CommandLine.Option(
            names = {"--output", "-o"},
            required = true)
    private Path outputFile;

    @CommandLine.Option(names = "--from")
    private String fromRaw;

    @CommandLine.Option(names = "--to")
    private String toRaw;

    public static void main(String[] args) {
        args = correctArgs(args);
        int exitCode = new CommandLine(new Application()).execute(args);
        exit(exitCode);
    }

    @Override
    public void run() {
        try {
            ArgumentsValidator.validate(paths, formatRaw, outputFile, fromRaw, toRaw);

            ReportFormat reportFormat = ReportFormat.valueOf(formatRaw.toUpperCase());
            Instant from = ArgumentsValidator.parseInstantOrNull(fromRaw);
            Instant to = ArgumentsValidator.parseInstantOrNull(toRaw);

            Arguments args = new Arguments(paths, reportFormat, outputFile, from, to);
            int exitCode = LogProcessingOrchestrator.run(args);
            exit(exitCode);

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            exit(2);
        } catch (Exception e) {
            log.error(e.getMessage());
            exit(1);
        }
    }

    private static String[] correctArgs(String[] args) {
        int start = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                start = i;
                break;
            }
        }
        return Arrays.copyOfRange(args, start, args.length);
    }

    // Note: нужно только для отладки, удалить в случае ненадобности
    @Deprecated(forRemoval = true)
    private static void debugArgs(List<String> args) {
        var argsPerParam = getArgumentsPerParameter(args);
        System.out.printf("Входные параметры программы: %s%n", argsPerParam);

        logPaths("Пути к лог-файлам", argsPerParam, "p", "path");
        logPaths("Пути к отчетам", argsPerParam, "o", "output");
    }

    private static Map<String, List<String>> getArgumentsPerParameter(List<String> args) {
        var argsPerParameter = new HashMap<String, List<String>>();
        argsPerParameter.put(UNDEFINED_PARAMETER, new ArrayList<>());

        var queue = new ArrayDeque<>(args);
        String currentParameter = null;
        while (!queue.isEmpty()) {
            var element = queue.removeFirst();
            if (element.startsWith("-")) {
                currentParameter = element.startsWith("--") ? element.substring(2) : element.substring(1);
                argsPerParameter.putIfAbsent(currentParameter, new ArrayList<>());
            } else {
                argsPerParameter
                        .get(Optional.ofNullable(currentParameter).orElse(UNDEFINED_PARAMETER))
                        .add(element);
            }
        }

        return argsPerParameter;
    }

    private static void logPaths(String description, Map<String, List<String>> argsPerParam, String... params) {
        var paths = new ArrayList<String>();
        for (var param : params) {
            paths.addAll(argsPerParam.getOrDefault(param, List.of()));
        }
        System.out.printf(
                "%s: %s%n",
                description,
                paths.stream()
                        .map(it -> it.contains("*")
                                ? "glob: " + it
                                : "path: %s, exists: %s".formatted(it, Files.exists(Path.of(it))))
                        .collect(Collectors.joining(";")));
    }
}
