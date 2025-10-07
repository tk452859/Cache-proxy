package org.example.caching_proxy.CACHECONTROLLER;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import org.example.caching_proxy.service.CacheService;
import org.example.caching_proxy.service.CacheStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/proxy")

public class Controller {
    @Autowired
    private  CacheService cacheService;
    @Autowired
    private  CacheStatsService cacheStatsService;

    // Make sure this constructor is present and properly defined


    @GetMapping("/**")
        public ResponseEntity<String> proxyRequest(HttpServletRequest request) {
            String path = extractPath(request);
            return cacheService.getCachedOrFetch(path);
        }

        @GetMapping("/cache/stats")
        public ResponseEntity<Map<String, Object>> getCacheStats() {
            return ResponseEntity.ok(cacheService.getCacheStats());
        }

        @GetMapping("/cache/stats/detailed")
        public ResponseEntity<Map<String, Object>> getDetailedCacheStats() {
            return ResponseEntity.ok(cacheService.getDetailedStats());
        }

        @PostMapping("/cache/clear")
        public ResponseEntity<Map<String, String>> clearCache() {
            cacheService.clearCache();
            return ResponseEntity.ok(Map.of("message", "Cache cleared successfully"));
        }

        @PostMapping("/cache/stats/reset")
        public ResponseEntity<Map<String, String>> resetStats() {
            cacheStatsService.resetStats();
            return ResponseEntity.ok(Map.of("message", "Cache statistics reset successfully"));
        }

        @PostMapping("/cache/clear/{path:.*}")
        public ResponseEntity<Map<String, String>> clearCacheEntry(@PathVariable String path) {
            cacheService.clearCacheEntry(path);
            return ResponseEntity.ok(Map.of("message", "Cache entry cleared for: " + path));
        }

        private String extractPath(HttpServletRequest request) {
            String requestUri = request.getRequestURI();
            return requestUri.replaceFirst("^/proxy", "");
        }

}
