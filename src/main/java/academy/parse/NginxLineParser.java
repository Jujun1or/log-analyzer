package academy.parse;

import academy.dto.LogEntry;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NginxLineParser implements LineParser {
    private static final Logger log = LogManager.getLogger(NginxLineParser.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(
                    "d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"));

    @Override
    public Optional<LogEntry> parseLine(String line) {
        try {
            Instant timestamp = extractTimestamp(line);
            String request = extractRequestRaw(line);
            String[] reqParts = splitRequest(request);

            String method = reqParts[0];
            String resource = reqParts[1];
            String protocol = reqParts[2];

            int[] statusAndBytes = extractStatusAndBytes(line, request);
            int status = statusAndBytes[0];
            int bodyBytes = statusAndBytes[1];

            return Optional.of(new LogEntry(timestamp, method, resource, protocol, status, bodyBytes));
        } catch (Exception e) {
            log.warn("Skipped NGINX log line: {} \t {}", line, e);
            return Optional.empty();
        }
    }

    private Instant extractTimestamp(String line) {
        int start = line.indexOf('[');
        int end = line.indexOf(']', start);
        if (start == -1 || end == -1) throw new IllegalArgumentException("Timestamp not found");

        String dateString = line.substring(start + 1, end);
        return Instant.from(TIME_FORMAT.parse(dateString));
    }

    private String extractRequestRaw(String line) {
        int start = line.indexOf('"');
        int end = line.indexOf('"', start + 1);
        if (start == -1 || end == -1) throw new IllegalArgumentException("Request not found");

        return line.substring(start + 1, end);
    }

    private String[] splitRequest(String raw) {
        String[] parts = raw.split(" ");
        if (parts.length != 3) throw new IllegalArgumentException("Incomplete request");
        return parts;
    }

    private int[] extractStatusAndBytes(String line, String request) {
        int reqEnd = line.indexOf('"') + request.length() + 1;
        String rest = line.substring(reqEnd + 1).trim();

        int space1 = rest.indexOf(' ');
        if (space1 == -1) throw new IllegalArgumentException("Missing status");

        int space2 = rest.indexOf(' ', space1 + 1);
        if (space2 == -1) throw new IllegalArgumentException("Missing body bytes sent");

        int status = Integer.parseInt(rest.substring(0, space1));
        int bodyBytes = Integer.parseInt(rest.substring(space1 + 1, space2));

        return new int[] {status, bodyBytes};
    }
}
