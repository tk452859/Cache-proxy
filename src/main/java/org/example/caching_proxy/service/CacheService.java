package org.example.caching_proxy.service;

import org.springframework.beans.factory.annotation.Value; // Correct import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {




        private final WebClient webClient;
        private final String targetServerUrl;
        private final CacheStatsService cacheStatsService;

        private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
        private final Duration cacheTtl = Duration.ofMinutes(10);
        private final int maxCacheSize = 1000;

        public CacheService(WebClient webClient,
                            @Value("${target.server.url:https://jsonplaceholder.typicode.com}") String targetServerUrl,
                            CacheStatsService cacheStatsService) {
            this.webClient = webClient;
            this.targetServerUrl = targetServerUrl;
            this.cacheStatsService = cacheStatsService;
        }

        public ResponseEntity<String> getCachedOrFetch(String path) {
            String cacheKey = generateCacheKey(path);

            // 1. CHECK CACHE
            CachedResponse cached = cache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                cacheStatsService.recordHit(path);
                System.out.println("✅ Cache HIT for: " + path);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .header("X-Cache", "HIT")
                        .header("X-Cache-Age", String.valueOf(cached.getAgeInSeconds()))
                        .body(cached.getBody());
            }

            cacheStatsService.recordMiss(path);
            System.out.println("❌ Cache MISS for: " + path);

            // 2. CALL REAL API
            String url = targetServerUrl + path;
            String responseBody = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        System.err.println("Error fetching from upstream: " + e.getMessage());
                        if (cached != null) {
                            System.out.println("⚠️  Using expired cache due to upstream error");
                            return Mono.just(cached.getBody());
                        }
                        return Mono.error(e);
                    })
                    .block();

            // 3. STORE IN CACHE
            if (responseBody != null) {
                cache.put(cacheKey, new CachedResponse(responseBody, Instant.now()));
                if (cache.size() > maxCacheSize) {
                    cleanupCache();
                }
            }

            // 4. RETURN RESPONSE
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .header("X-Cache", responseBody != null ? "MISS" : "STALE")
                    .body(responseBody);
        }

        public void clearCache() {
            cache.clear();
            cacheStatsService.recordClear();
            System.out.println("🗑️  Cache cleared");
        }

        public void clearCacheEntry(String path) {
            String cacheKey = generateCacheKey(path);
            cache.remove(cacheKey);
            System.out.println("🗑️  Cache entry cleared: " + path);
        }

        // Delegate stats to CacheStatsService
        public Map<String, Object> getCacheStats() {
            return cacheStatsService.getOverallStats();
        }

        public Map<String, Object> getDetailedStats() {
            return cacheStatsService.getDetailedStats();
        }

        private String generateCacheKey(String path) {
            return "cache:" + path;
        }

        private void cleanupCache() {
            System.out.println("🧹 Cleaning up cache, current size: " + cache.size());
            cache.entrySet().removeIf(entry ->
                    entry.getValue().isExpired() || cache.size() > maxCacheSize
            );
        }

        private class CachedResponse {
            private final String body;
            private final Instant cachedAt;

            public CachedResponse(String body, Instant cachedAt) {
                this.body = body;
                this.cachedAt = cachedAt;
            }

            public boolean isExpired() {
                return Duration.between(cachedAt, Instant.now()).compareTo(cacheTtl) > 0;
            }

            public long getAgeInSeconds() {
                return Duration.between(cachedAt, Instant.now()).getSeconds();
            }

            public String getBody() {
                return body;
            }
        }

}
