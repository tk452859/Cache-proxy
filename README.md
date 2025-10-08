🚀 Cache Proxy with Analytics Dashboard

A high-performance caching proxy built with Spring Boot that dramatically improves API response times while providing real-time performance analytics.

🌐 Live Demo

⚡ API Demo: Test proxied endpoints with caching

Experience the cache proxy in action:

🔗 Live Dashboard: https://cache-proxy-5.onrender.com/

📊 Real-time Metrics: Watch cache performance live






✨ Features

⚡ High-Performance Caching: In-memory caching with 90%+ hit rates

📊 Real-time Analytics: Live dashboard with performance metrics

🔒 Thread-Safe: Concurrent caching using ConcurrentHashMap

⏰ Smart Eviction: TTL-based cache expiration with automatic cleanup

🌐 RESTful API: Clean endpoints for cache management and monitoring

🚀 Production Ready: Deployed and serving live traffic

🎯 Performance Impact

Metric	Before Caching	 After Caching	Improvement

Response Time	200-300ms	 5-10ms	      20x faster

External API Calls	     100%	        10%	 90% reduction

Cache Hit Rate	          -	          90%+	Optimal efficiency

🏗️ Architecture

Client Request → Cache Proxy → [Cache Hit → Immediate Response]
                              [Cache Miss → External API → Store → Response]
                              
Real-time Metrics → Analytics Dashboard → Performance Insights

Prerequisites

Java 17+

Maven 3.6+

Spring Boot 2.7+

Usage

# Access dashboard

http://localhost:8080/

# Proxy API calls

http://localhost:8080/proxy/posts

http://localhost:8080/proxy/users/1

# Cache management

http://localhost:8080/proxy/cache/stats

http://localhost:8080/proxy/cache/clear

📊 API Endpoints

Proxy Endpoints

GET /proxy/{path} - Proxy requests to external API with caching

GET /proxy/posts - Example: Fetch posts with caching

GET /proxy/users/{id} - Example: Fetch user data with caching

Cache Management

GET /proxy/cache/stats - Get cache statistics (hits, misses, hit rate)

GET /proxy/cache/stats/detailed - Detailed endpoint-level statistics

POST /proxy/cache/clear - Clear entire cache

POST /proxy/cache/clear/{path} - Clear specific cache entry

Monitoring

GET /health - Application health check

Dashboard: http://localhost:8080/ - Real-time analytics UI

📈 Performance Metrics

The system tracks and displays:

Cache Hit Rate: Percentage of requests served from cache

Total Requests: Overall request volume

Response Times: Average and p95 response times

Endpoint Performance: Individual endpoint cache efficiency

Memory Usage: Cache size and memory utilization

🔧 Technical Implementation

Core Components

CacheService: Thread-safe caching logic with TTL eviction

ProxyController: REST endpoints for proxying and cache management

CacheStatsService: Real-time metrics collection and analytics

WebClient: Non-blocking HTTP client for external API calls






