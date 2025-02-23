package com.pharmacyhub.security.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class RBACCacheConfig {

    @Bean
    public CacheManager rbacCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("userPermissions"),
            new ConcurrentMapCache("userRoles"),
            new ConcurrentMapCache("roleHierarchy"),
            new ConcurrentMapCache("groupRoles")
        ));
        return cacheManager;
    }
}