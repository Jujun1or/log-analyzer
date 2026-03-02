package academy.io;

import academy.dto.ResolvedSource;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LogSourceResolver {

    private LogSourceResolver() {}

    public static List<ResolvedSource> resolve(List<String> sources) {
        List<ResolvedSource> resolvedSources = new ArrayList<>();

        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("No --path provided");
        }

        for (String source : sources) {
            if (source == null || source.isBlank()) {
                throw new IllegalArgumentException("Empty --path value");
            }

            if (isUri(source)) {
                resolvedSources.add(ResolvedSource.createRemote(URI.create(source)));
            } else if (isGlob(source)) {
                expandGlob(source, resolvedSources);
            } else {
                resolvedSources.add(ResolvedSource.createLocal(Path.of(source)));
            }
        }

        if (resolvedSources.isEmpty()) {
            throw new IllegalArgumentException("No matched files parsed from --path");
        }

        return resolvedSources;
    }

    private static void expandGlob(String pattern, List<ResolvedSource> out) {
        int lastSlash = pattern.lastIndexOf('/');
        Path dir;
        String filePattern;

        if (lastSlash >= 0) {
            String dirPart = pattern.substring(0, lastSlash);
            filePattern = pattern.substring(lastSlash + 1);
            dir = dirPart.isEmpty() ? Path.of(".") : Path.of(dirPart);
        } else {
            dir = Path.of(".");
            filePattern = pattern;
        }

        if (!Files.isDirectory(dir)) {
            return;
        }

        if (filePattern.contains("**")) {
            try (var stream = Files.walk(dir)) {
                PathMatcher matcher = dir.getFileSystem().getPathMatcher("glob:" + pattern);

                stream.filter(Files::isRegularFile)
                        .filter(LogSourceResolver::isSupportedLogOrTxt)
                        .filter(p -> matcher.matches(dir.relativize(p)))
                        .forEach(p -> out.add(
                                ResolvedSource.createLocal(p.toAbsolutePath().normalize())));

            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to expand glob: " + pattern, e);
            }

            return;
        }

        PathMatcher matcher = dir.getFileSystem().getPathMatcher("glob:" + filePattern);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {

                if (Files.isDirectory(entry)) {
                    continue;
                }

                if (!isSupportedLogOrTxt(entry)) {
                    continue;
                }

                if (matcher.matches(entry.getFileName())) {
                    out.add(ResolvedSource.createLocal(entry.toAbsolutePath().normalize()));
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to expand glob: " + pattern, e);
        }
    }

    private static boolean isSupportedLogOrTxt(Path path) {
        if (path == null) {
            return false;
        }

        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }

        String lower = fileName.toString().toLowerCase(Locale.ROOT);
        return lower.endsWith(".log") || lower.endsWith(".txt");
    }

    private static boolean isUri(String source) {
        return source.startsWith("http://") || source.startsWith("https://");
    }

    private static boolean isGlob(String source) {
        return source.contains("*") || source.contains("?") || source.contains("[") || source.contains("]");
    }
}
