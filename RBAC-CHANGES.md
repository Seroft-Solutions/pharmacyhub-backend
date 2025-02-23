# Enhanced Role-Based Access Control (RBAC) Implementation

## Overview
This document details the implementation of an enhanced Role-Based Access Control (RBAC) system in the PharmacyHub backend application. The system includes user roles, permissions, groups, and audit logging capabilities.

## Major Changes

### 1. Dependency Updates
- Removed Keycloak and Redis dependencies
- Added Flyway for database migrations
- Retained core Spring Security and JWT dependencies

### 2. Security Model

#### Entities
- **User**: Extended with groups and enhanced role relationships
- **Role**: Enhanced with permissions and role hierarchy
- **Permission**: New entity for granular access control
- **Group**: New entity for role grouping
- **AuditLog**: New entity for security event tracking

#### Database Schema Changes
```sql
- permissions (id, name, description)
- groups (id, name, description)
- role_permissions (role_id, permission_id)
- role_hierarchy (parent_role_id, child_role_id)
- group_roles (group_id, role_id)
- user_groups (user_id, group_id)
- audit_logs (id, action, details, user_id, timestamp, etc.)
```

### 3. Security Configuration

#### JWT Authentication
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // JWT Filter chain configuration
    // Method security with custom permission evaluator
    // CORS configuration
    // Authentication manager setup
}
```

#### Permission Evaluation
```java
@PreAuthorize("hasRole('ADMIN') or hasPermission(null, 'VIEW_PHARMACIST')")
public PharmacistDTO findUser(long id) {
    // Method implementation
}
```

### 4. Available Permissions
```java
public enum PermissionEnum {
    CREATE_PHARMACIST,
    UPDATE_PHARMACIST,
    VIEW_PHARMACIST,
    VIEW_ALL_PHARMACISTS,
    MANAGE_CONNECTIONS,
    VIEW_CONNECTIONS,
    VIEW_ALL_CONNECTIONS,
    // ... more permissions
}
```

### 5. Database Migration
Flyway migrations are set up to manage database schema changes:
- V1__init_schema.sql: Baseline schema
- V2__add_rbac_tables.sql: RBAC-related tables

### 6. Default Role Permissions

#### Admin Role
- Has access to all permissions
- Can manage roles, permissions, and groups
- Full access to audit logs

#### Pharmacist Role
- VIEW_PHARMACIST
- UPDATE_PHARMACIST
- MANAGE_CONNECTIONS
- VIEW_CONNECTIONS

#### Proprietor Role
- VIEW_PHARMACIST
- VIEW_ALL_PHARMACISTS
- MANAGE_CONNECTIONS
- VIEW_CONNECTIONS
- VIEW_ALL_CONNECTIONS

## Configuration Files

### application.yml
```yaml
spring:
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/pharmacyhub}
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: ${JWT_EXPIRATION:18000}
```

## Services

### PermissionService
- Manages permission assignments
- Checks user permissions
- Handles permission inheritance

### GroupService
- Manages group creation and updates
- Handles role assignments to groups
- Manages user group memberships

### AuditService
- Logs security events
- Tracks user actions
- Provides audit trail capabilities

## Usage Examples

### Controller Level Security
```java
@RestController
@RequestMapping("/api/v1/pharmacists")
public class PharmacistController {
    
    @PreAuthorize("hasRole('ADMIN') or hasPermission(null, 'VIEW_PHARMACIST')")
    @GetMapping("/{id}")
    public ResponseEntity<PharmacistDTO> getPharmacist(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacistService.findUser(id));
    }
}
```

### Service Level Security
```java
@Service
public class PharmacistService {
    
    @PreAuthorize("hasRole('ADMIN') or hasPermission(null, 'CREATE_PHARMACIST')")
    public PharmacistDTO saveUser(PharmacistDTO dto) {
        // Implementation
    }
}
```

## Security Flow

1. **Authentication**:
   - User submits credentials
   - JWT token generated with roles and permissions
   - Token includes user details and authorities

2. **Authorization**:
   - JWT token validated on each request
   - Permissions checked at method/endpoint level
   - Custom permission evaluator handles complex checks

3. **Audit Logging**:
   - Security events logged to database
   - User actions tracked with timestamps
   - IP and user agent information recorded

## Maintenance

### Adding New Permissions
1. Add to PermissionEnum
2. Create migration script if needed
3. Update role assignments in PermissionSeeder
4. Add security annotations where needed

### Managing Roles
1. Use admin interface to assign permissions
2. Update role hierarchy if needed
3. Manage group assignments

## Future Enhancements

1. **Cache Layer**:
   - Add caching for frequently accessed permissions
   - Cache user authorities to reduce database calls

2. **API Extensions**:
   - Bulk permission management
   - Role hierarchy visualization
   - Advanced audit log filtering

3. **Security Hardening**:
   - Rate limiting
   - IP-based restrictions
   - Enhanced password policies