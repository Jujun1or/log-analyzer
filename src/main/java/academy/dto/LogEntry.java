package academy.dto;

import java.time.Instant;

public record LogEntry(Instant timestamp, String method, String resource,
                       String protocol, int status, int responseSize) {
}
