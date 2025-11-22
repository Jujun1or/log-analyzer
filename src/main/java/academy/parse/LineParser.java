package academy.parse;

import academy.dto.LogEntry;
import java.util.Optional;

public interface LineParser {
    Optional<LogEntry> parseLine(String line);
}
