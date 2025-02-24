# PharmacyHub RBAC System Enhancements

## Overview

This document outlines the enhancements made to the Role-Based Access Control (RBAC) system in the PharmacyHub backend application. These improvements strengthen security, add more granular access control, and improve audit logging capabilities.

## Key Enhancements

### 1. Improved Permission Evaluation

- Enhanced the `PHPermissionEvaluator` to provide more granular permission checking
- Added support for generic permission checks without specific target objects
- Implemented targeted permission evaluation with resource type and operation matching
- Integrated audit logging during permission checks for better traceability

### 2. Enhanced RBAC Service

- Added proper exception handling with specific error codes and messages
- Implemented cache management with appropriate cache evictions
- Added permission inheritance through role hierarchies
- Implemented user permission overrides with both grant and deny capabilities
- Added group-based permission management
- Added methods for querying users by roles, groups, and permissions
- Added comprehensive validation for resource access

### 3. Validation and Error Handling

- Implemented `RBACValidationService` for validating role, permission, and group operations
- Added circular dependency detection in role hierarchies
- Added validation for resource and operation type combinations
- Implemented proper exception handling with specific error codes

### 4. Audit Logging

- Enhanced audit logging with detailed information about security events
- Added user context to audit logs
- Added outcome (SUCCESS/DENIED) to audit logs
- Implemented logging for permission checks, role assignments, and other security-related operations

### 5. User Repository Extensions

- Added methods for finding users by roles and groups
- Added methods for finding users with specific permissions
- Added support for checking permission overrides

### 6. Comprehensive Testing

- Created integration tests for the RBAC system
- Added JSON test fixtures for different access control scenarios
- Implemented tests for role hierarchy, group-based permissions, and permission overrides
- Added dynamic access control tests based on resource attributes

## Implementation Details

### Permission Inheritance

Permissions are inherited through:
1. Direct role assignments
2. Group memberships
3. Role hierarchies
4. Permission overrides

The system computes the effective permissions by:
1. Collecting permissions from directly assigned roles
2. Adding permissions from parent roles in the hierarchy
3. Adding permissions from roles assigned to groups the user belongs to
4. Applying permission overrides (both grants and denials)

### Cache Management

The system uses caching to improve performance:
- `userPermissions`: Caches effective permissions for users
- `userRoles`: Caches all roles assigned to users (directly and through groups)
- `roleHierarchy`: Caches role hierarchies to avoid traversing the tree repeatedly
- `groupRoles`: Caches roles assigned to groups
- `userHasPermission` and `userHasRole`: Caches specific permission and role checks

Cache eviction is managed carefully during operations that modify roles, permissions, or group assignments.

### Security Annotations

The system uses Spring Security's method-level security annotations:
```java
@PreAuthorize("hasPermission('ROLE', 'MANAGE')")
public Role createRole(RoleDTO roleDTO) {
    // Implementation
}
```

### Role Hierarchy

The system supports role hierarchies where parent roles inherit permissions from child roles:
```java
// Example: ADMIN -> PHARMACY_MANAGER -> SENIOR_PHARMACIST -> PHARMACIST
```

A permission granted to a PHARMACIST is automatically available to SENIOR_PHARMACIST, PHARMACY_MANAGER, and ADMIN.

## Usage Examples

### Basic Permission Check
```java
boolean hasAccess = rbacService.validateAccess(userId, "MEDICINE", "READ", medicineId);
```

### Assigning Roles and Groups
```java
rbacService.assignRoleToUser(userId, roleId);
rbacService.assignGroupToUser(userId, groupId);
```

### Permission Overrides
```java
// Grant a specific permission
rbacService.addPermissionOverride(userId, "APPROVE_PRESCRIPTION", true);

// Deny a specific permission
rbacService.addPermissionOverride(userId, "DELETE_MEDICINE", false);
```

### Finding Users with Specific Access
```java
List<User> approvers = rbacService.getUsersByPermission("APPROVE_PRESCRIPTION");
List<User> pharmacists = rbacService.getUsersByRole("PHARMACIST");
```

## Conclusion

These enhancements significantly improve the security posture of the PharmacyHub application by providing:

1. More granular access control
2. Better audit traceability
3. Flexible permission management through roles, groups, and overrides
4. Improved performance through intelligent caching
5. Comprehensive validation of security operations

These changes maintain backward compatibility with existing functionality while adding new capabilities to support more sophisticated access control requirements.
