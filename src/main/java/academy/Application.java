package academy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "Application Example", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String UNDEFINED_PARAMETER = "undefined";

    public static void main(String[] args) {
        // Логирование входных параметров для проверки работоспособности black-box тестов
        debugArgs(Arrays.asList(args));

        // Запуск программы
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // реализуйте логику по парсингу лог-файлов :)
    }

    // Note: нужно только для отладки, удалить в случае ненадобности
    @Deprecated(forRemoval = true)
    private static void debugArgs(List<String> args) {
        var argsPerParam = getArgumentsPerParameter(args);
        logger.debug("Входные параметры программы: {}", argsPerParam);

        logPaths("Пути к лог-файлам", argsPerParam, "p", "path");
        logPaths("Пути к отчетам", argsPerParam, "o", "output");
    }

    private static Map<String, List<String>> getArgumentsPerParameter(List<String> args) {
        var argsPerParameter = new HashMap<String, List<String>>();
        argsPerParameter.put(UNDEFINED_PARAMETER, new ArrayList<>());

        var queue = new ArrayDeque<>(args);
        String currentParameter = null;
        while (!queue.isEmpty()) {
            var element = args.removeFirst();
            if (element.startsWith("-")) {
                currentParameter = element.startsWith("--") ? element.substring(2) : element.substring(1);
                argsPerParameter.putIfAbsent(currentParameter, new ArrayList<>());
            } else {
                argsPerParameter.get(
                        Optional.ofNullable(currentParameter)
                            .orElse(UNDEFINED_PARAMETER))
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
        logger.debug("{}: {}",
            description, paths.stream()
                .map(it ->
                    it.contains("*")
                        ? "glob: " + it
                        : "path: %s, exists: %s".formatted(it, Files.exists(Path.of(it))))
                .collect(Collectors.joining(";")));
    }
}
