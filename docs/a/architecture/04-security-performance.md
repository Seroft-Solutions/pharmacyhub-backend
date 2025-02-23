# RBAC Performance Optimization Guide

## Overview

This document provides guidance on optimizing the performance of PharmacyHub's RBAC system. It covers caching strategies, database optimization, and best practices for efficient permission evaluation.

## Caching Strategy

### 1. Permission Cache
```java
@Configuration
@EnableCaching
public class RBACCacheConfig {
    @Bean
    public CacheManager rbacCacheManager() {
        return new ConcurrentMapCacheManager(
            "userPermissions",    // User's effective permissions
            "roleHierarchy",      // Role hierarchy relationships
            "groupRoles",         // Group role assignments
            "userRoles"          // User role assignments
        );
    }
}
```

### 2. Cache Usage
```java
@Service
public class RBACService {
    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<Permission> getUserEffectivePermissions(Long userId) {
        // Permission calculation logic
    }

    @CacheEvict(value = "userPermissions", key = "#userId")
    public void clearUserPermissionCache(Long userId) {
        // Cache eviction logic
    }
}
```

## Database Optimization

### 1. Index Strategy
```sql
-- Role table indexes
CREATE INDEX idx_role_name ON roles(name);
CREATE INDEX idx_role_precedence ON roles(precedence);

-- Permission table indexes
CREATE INDEX idx_permission_name ON permissions(name);
CREATE INDEX idx_permission_resource ON permissions(resource_type);

-- User role mapping indexes
CREATE INDEX idx_user_roles ON user_roles(user_id, role_id);
```

### 2. Query Optimization
```java
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r " +
           "LEFT JOIN FETCH r.permissions " +
           "LEFT JOIN FETCH r.childRoles " +
           "WHERE r.id = :roleId")
    Optional<Role> findByIdWithRelations(Long roleId);
}
```

## Permission Evaluation

### 1. Fast Path Evaluation
```java
public class PermissionEvaluator {
    public boolean hasPermission(User user, String resource, String operation) {
        // Check cache first
        Set<Permission> cachedPermissions = permissionCache.get(user.getId());
        if (cachedPermissions != null) {
            return evaluateFromCache(cachedPermissions, resource, operation);
        }

        // Fall back to database
        return evaluateFromDatabase(user, resource, operation);
    }
}
```

### 2. Batch Permission Loading
```java
@Service
public class PermissionLoader {
    public Map<Long, Set<Permission>> loadPermissionsForUsers(Set<Long> userIds) {
        return userIds.stream()
            .collect(Collectors.toMap(
                userId -> userId,
                this::getUserEffectivePermissions
            ));
    }
}
```

## Role Hierarchy Optimization

### 1. Hierarchy Traversal
```java
public class RoleHierarchyManager {
    private Map<Long, Set<Long>> hierarchyCache = new ConcurrentHashMap<>();

    public Set<Role> getInheritedRoles(Long roleId) {
        Set<Long> inheritedIds = hierarchyCache.get(roleId);
        if (inheritedIds == null) {
            inheritedIds = calculateInheritedRoles(roleId);
            hierarchyCache.put(roleId, inheritedIds);
        }
        return roleRepository.findAllById(inheritedIds);
    }
}
```

### 2. Precedence-Based Optimization
```java
public class RoleManager {
    public boolean canAssignRole(User assigner, User assignee, Role roleToAssign) {
        // Quick check based on role precedence
        return assigner.getHighestRolePrecedence() < 
               roleToAssign.getPrecedence();
    }
}
```

## Memory Management

### 1. Cache Size Control
```java
@Configuration
public class CacheConfiguration {
    @Bean
    public CacheManager cacheManager() {
        GuavaCache userPermissions = new GuavaCache(
            "userPermissions",
            CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build()
        );
        
        return new SimpleCacheManager(
            Arrays.asList(userPermissions)
        );
    }
}
```

### 2. Memory-Efficient Data Structures
```java
public class PermissionSet {
    private final BitSet permissions;
    private final int[] resourceMap;

    public boolean hasPermission(int resourceId, int operationId) {
        int bitIndex = resourceMap[resourceId] + operationId;
        return permissions.get(bitIndex);
    }
}
```

## Monitoring and Metrics

### 1. Performance Metrics
```java
@Component
public class RBACMetrics {
    private final MeterRegistry registry;

    public void recordPermissionCheck(long startTime) {
        registry.timer("rbac.permission.check")
            .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    }

    public void recordCacheHit(String cache) {
        registry.counter("rbac.cache.hit", "cache", cache)
            .increment();
    }
}
```

### 2. Performance Alerts
```java
@Component
public class PerformanceMonitor {
    @Scheduled(fixedRate = 5000)
    public void checkPerformance() {
        double p99LatencyMs = getPermissionCheckLatency(0.99);
        if (p99LatencyMs > 100) {
            alertService.sendAlert(
                "High RBAC latency detected: " + p99LatencyMs + "ms"
            );
        }
    }
}
```

## Best Practices

### 1. Permission Design
- Keep permissions granular but not too fine-grained
- Use role hierarchy effectively
- Cache frequently accessed permissions
- Optimize permission checks

### 2. Database Access
- Use appropriate indexes
- Batch related queries
- Minimize cross-table joins
- Cache query results

### 3. Cache Management
- Set appropriate cache sizes
- Define clear eviction policies
- Monitor cache hit rates
- Update cache strategically

### 4. Memory Usage
- Control cache sizes
- Use efficient data structures
- Implement pagination
- Clean up unused data

## Performance Testing

### 1. Load Testing
```java
@Test
public void permissionCheckUnderLoad() {
    // Simulate 1000 concurrent permission checks
    ExecutorService executor = Executors.newFixedThreadPool(10);
    for (int i = 0; i < 1000; i++) {
        executor.submit(() -> {
            rbacService.hasPermission(user, "INVENTORY", "READ");
        });
    }
}
```

### 2. Monitoring Tests
```java
@Test
public void monitorCachePerformance() {
    Metrics metrics = new Metrics();
    for (int i = 0; i < 10000; i++) {
        long start = System.nanoTime();
        rbacService.getUserEffectivePermissions(userId);
        metrics.recordLatency(System.nanoTime() - start);
    }
    
    assertThat(metrics.getP95Latency())
        .isLessThan(Duration.ofMillis(10));
}
```