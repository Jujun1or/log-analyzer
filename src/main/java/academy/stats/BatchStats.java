package academy.stats;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public record BatchStats(
   long totalRequests,
   long totalBytes,
   int maxBytes,
   Map<Integer, Long> responseSizeFreq,
   Map<Integer, Long> responseCade,
   Map<String, Long> resources,
   Map<LocalDate, Long> requestsByDate,
   Set<String> uniqueProtocols
) {}
