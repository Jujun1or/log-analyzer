package academy.dto;

import academy.enums.ReportFormat;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public record Arguments(List<String> paths, ReportFormat reportFormat,
                        Path output, Instant from, Instant to) {}
