package academy;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "Application Example", version = "Example 1.0", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // реализуйте логику по парсингу лог-файлов :)
    }
}
