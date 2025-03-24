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
 * Note: The actual cache manager bean is now defined in RBACCacheConfig as rbacCacheManager to avoid conflicts.
 * This class is kept for documentation purposes.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final int CACHE_TTL_SECONDS = 300; // 5 minutes TTL for caches
    
    // Cache definitions have been moved to RBACCacheConfig to avoid bean conflicts
}
