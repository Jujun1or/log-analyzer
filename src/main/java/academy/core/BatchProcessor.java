package academy.core;

import academy.dto.Batch;
import academy.dto.LogEntry;
import academy.stats.BatchStats;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BatchProcessor {

    public BatchStats process(Batch batch) {

        long totalRequests = 0;
        long totalBytes = 0;
        int maxBytes = 0;

        Map<Integer, Long> responseSizeFreq = new HashMap<>();
        Map<Integer, Long> responseCode = new HashMap<>();
        Map<String, Long> resources = new HashMap<>();
        Map<LocalDate, Long> requestsByDate = new HashMap<>();
        Set<String> uniqueProtocols = new HashSet<>();

        for (LogEntry entry : batch.entries()) {
            totalRequests++;
            totalBytes += entry.responseSize();
            maxBytes = Math.max(maxBytes, entry.responseSize());

            updateFrequency(responseSizeFreq, entry.responseSize());
            updateFrequency(responseCode, entry.status());
            updateFrequency(resources, entry.resource());

            LocalDate date = toLocalDateUTC(entry.timestamp());
            updateFrequency(requestsByDate, date);

            uniqueProtocols.add(entry.protocol());
        }

        return new BatchStats(
            totalRequests,
            totalBytes,
            maxBytes,
            responseSizeFreq,
            responseCode,
            resources,
            requestsByDate,
            uniqueProtocols
        );
    }

    private <K> void updateFrequency(Map<K, Long> map, K key) {
        map.merge(key, 1L, Long::sum);
    }

    private static LocalDate toLocalDateUTC(java.time.Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
