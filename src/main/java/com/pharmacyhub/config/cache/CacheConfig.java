package com.pharmacyhub.config.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration for application caching
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final int CACHE_TTL_SECONDS = 300; // 5 minutes TTL for caches
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            // Payment caches
            new ConcurrentMapCache("userPaymentRequests"),
            new ConcurrentMapCache("pendingRequestCheck"),
            new ConcurrentMapCache("anyPendingRequestCheck"),
            new ConcurrentMapCache("approvedRequestCheck"),
            
            // Exam caches
            new ConcurrentMapCache("examTitles"),
            new ConcurrentMapCache("examDetails")
        ));
        return cacheManager;
    }
}
