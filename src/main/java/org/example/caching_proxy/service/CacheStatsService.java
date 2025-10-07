package org.example.caching_proxy.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CacheStatsService {

    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final Map<String, AtomicLong> endpointHits = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> endpointMisses = new ConcurrentHashMap<>();

    public void recordHit(String path) {
        totalHits.incrementAndGet();
        endpointHits.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void recordMiss(String path) {
        totalMisses.incrementAndGet();
        endpointMisses.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void recordClear() {
        // You could track clear operations if needed
    }

    public Map<String, Object> getOverallStats() {
        long hits = totalHits.get();
        long misses = totalMisses.get();
        long totalRequests = hits + misses;
        double hitRate = totalRequests > 0 ? (double) hits / totalRequests * 100 : 0;

        return Map.of(
                "totalHits", hits,
                "totalMisses", misses,
                "totalRequests", totalRequests,
                "hitRate", String.format("%.2f%%", hitRate),
                "endpointCount", endpointHits.size()
        );
    }

    public Map<String, Object> getDetailedStats() {
        Map<String, Object> detailedStats = new ConcurrentHashMap<>();
        Map<String, Map<String, Object>> endpointStats = new ConcurrentHashMap<>();

        // Combine hits and misses for each endpoint
        for (String endpoint : endpointHits.keySet()) {
            long hits = endpointHits.getOrDefault(endpoint, new AtomicLong(0)).get();
            long misses = endpointMisses.getOrDefault(endpoint, new AtomicLong(0)).get();
            long total = hits + misses;
            double hitRate = total > 0 ? (double) hits / total * 100 : 0;

            endpointStats.put(endpoint, Map.of(
                    "hits", hits,
                    "misses", misses,
                    "totalRequests", total,
                    "hitRate", String.format("%.2f%%", hitRate)
            ));
        }

        detailedStats.put("overall", getOverallStats());
        detailedStats.put("endpoints", endpointStats);

        return detailedStats;
    }

    public void resetStats() {
        totalHits.set(0);
        totalMisses.set(0);
        endpointHits.clear();
        endpointMisses.clear();
    }

}
