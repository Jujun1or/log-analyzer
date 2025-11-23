package academy.cli;

import academy.enums.ReportFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class ArgumentsValidator {

    private ArgumentsValidator() {
    }

    public static void validate(List<String> paths, String formatRaw, Path output,
                                String fromRaw, String toRaw) {
        validatePaths(paths);

        if (!isSupportedFormat(formatRaw)) {
            throw new IllegalArgumentException("Unsupported output format: " + formatRaw);
        }

        if (output == null) {
            throw new IllegalArgumentException("Output file is required");
        }

        if (Files.exists(output)) {
            throw new IllegalArgumentException("Output file already exists: " + output);
        }

        Path parent = output.toAbsolutePath().getParent();
        if (parent == null || !Files.exists(parent) || !Files.isWritable(parent)) {
            throw new IllegalArgumentException("Output directory is not writable: " + parent);
        }

        validateOutputExtension(output, formatRaw);

        Instant from = parseInstantOrNull(fromRaw);
        Instant to = parseInstantOrNull(toRaw);

        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("--from must be earlier than --to");
        }
    }

    private static void validatePaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("No --path provided");
        }

        for (String p : paths) {
            if (p == null || p.isBlank()) {
                throw new IllegalArgumentException("Empty --path value");
            }

            if (isUrl(p)) {
                continue;
            }

            if (p.endsWith("/")) {
                throw new IllegalArgumentException("Path is a directory: " + p);
            }

            if (isGlob(p)) {
                continue;
            }

            if (!Files.exists(Path.of(p))) {
                throw new IllegalArgumentException("Output file already exists: " + p);
            }

            String lower = p.toLowerCase();
            if (!(lower.endsWith(".log") || lower.endsWith(".txt"))) {
                throw new IllegalArgumentException("Unsupported file format: " + p);
            }
        }
    }

    private static void validateOutputExtension(Path output, String formatRaw) {
        String lower = output.toString().toLowerCase();

        switch (formatRaw) {
            case "json" -> {
                if (!lower.endsWith(".json")) {
                    throw new IllegalArgumentException("Output file must have .json extension");
                }
            }
            case "markdown" -> {
                if (!lower.endsWith(".md")) {
                    throw new IllegalArgumentException("Output file must have .md extension");
                }
            }
            case "adoc" -> {
                if (!lower.endsWith(".adoc") && !lower.endsWith(".ad")) {
                    throw new IllegalArgumentException("Output file must have .adoc extension");
                }
            }
        }
    }

    private static boolean isSupportedFormat(String raw) {
        if (raw == null) return false;
        for (var format : ReportFormat.values()) {
            if (String.valueOf(format).equalsIgnoreCase(raw)) return true;
        }
        return false;
    }

    public static Instant parseInstantOrNull(String raw) {
        if (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw)) {
            return null;
        }

        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException ignore) {}

        try {
            LocalDate date = LocalDate.parse(raw);
            return date.atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid ISO-8601 date: " + raw);
        }
    }

    private static boolean isUrl(String s) {
        return s.startsWith("http://") || s.startsWith("https://");
    }

    private static boolean isGlob(String s) {
        return s.contains("*") || s.contains("?") || s.contains("[") || s.contains("]");
    }
}
