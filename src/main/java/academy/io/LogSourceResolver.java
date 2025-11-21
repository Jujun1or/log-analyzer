package academy.io;

import academy.dto.ResolvedSource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class LogSourceResolver {

    public static List<ResolvedSource> resolve(List<String> sources) {
        List<ResolvedSource> resolvedSources = new ArrayList<>();

        for (String source : sources) {

            if (isUri(source)) {
                resolvedSources.add(ResolvedSource.createRemote(URI.create(source)));
                continue;
            }

            if (isGlob(source)) {
                int lastSlashIndex = source.lastIndexOf('/');

                String directory;
                String pattern;

                if (lastSlashIndex != -1) {
                    directory = source.substring(0, lastSlashIndex);
                    pattern = source.substring(lastSlashIndex + 1);
                } else {
                    directory = ".";
                    pattern = source;
                }

                List<Path> matches = expandGlob(directory, pattern);
                for (Path p : matches) {
                    resolvedSources.add(ResolvedSource.createLocal(p));
                }

                continue;
            }

            Path p = Path.of(source);
            resolvedSources.add(ResolvedSource.createLocal(p));
        }

        if (resolvedSources.isEmpty()) {
            throw new IllegalArgumentException("No matched files parsed from --path");
        }

        return resolvedSources;
    }


    private static List<Path> expandGlob(String directory, String pattern) {
        List<Path> result = new ArrayList<>();

        Path dir = directory.isBlank() ? Path.of(".") : Path.of(directory);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Directory does not exist: " + directory);
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (!matcher.matches(entry.getFileName())) {
                    continue;
                }

                if (Files.isDirectory(entry)) {
                    continue;
                }

                String name = entry.getFileName().toString().toLowerCase();

                if (name.endsWith(".log") || name.endsWith(".txt")) {
                    result.add(entry.toAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process glob: " + directory + "/" + pattern, e);
        }

        return result;
    }


    private static boolean isUri(String source) {
        return source.startsWith("http://") || source.startsWith("https://");
    }

    private static boolean isGlob(String source) {
        return source.contains("*") || source.contains("?")
            || source.contains("[") || source.contains("]");
    }
}
