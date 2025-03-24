package com.pharmacyhub.security.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;

@Configuration
@EnableCaching
public class RBACCacheConfig {

    @Bean
    @Primary
    public CacheManager rbacCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            // RBAC caches
            new ConcurrentMapCache("userPermissions"),
            new ConcurrentMapCache("userRoles"),
            new ConcurrentMapCache("roleHierarchy"),
            new ConcurrentMapCache("groupRoles"),
            
            // Feature access caches
            new ConcurrentMapCache("featureAccess"),
            new ConcurrentMapCache("featureOperations"),
            new ConcurrentMapCache("featureOperationAccess"),
            new ConcurrentMapCache("userFeatures"),
            
            // User permission caches
            new ConcurrentMapCache("userHasPermission"),
            new ConcurrentMapCache("userHasRole"),
            
            // Payment caches (added from CacheConfig to prevent conflicts)
            new ConcurrentMapCache("userPaymentRequests"),
            new ConcurrentMapCache("pendingRequestCheck"),
            new ConcurrentMapCache("anyPendingRequestCheck"),
            new ConcurrentMapCache("approvedRequestCheck"),
            
            // Exam caches (added from CacheConfig to prevent conflicts)
            new ConcurrentMapCache("examTitles"),
            new ConcurrentMapCache("examDetails")
        ));
        return cacheManager;
    }
}