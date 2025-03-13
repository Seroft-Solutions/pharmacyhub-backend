# Directory-Based User Types Implementation

This document outlines the directory-based approach for implementing different user types in the PharmacyHub application.

## Overview

The implementation uses a structured directory approach to organize code related to user types:

```
com.pharmacyhub.security.users/
  ├── factory/
  │   └── UserTypeFactory.java       # Factory for creating different user types
  ├── initializer/
  │   └── DefaultUsersInitializer.java  # Initialize default users of all types
  ├── groups/
  │   └── GroupSeeder.java           # Configure and seed user groups
  ├── service/
  │   └── UserTypeService.java       # Service for user type-specific operations
  ├── controller/
  │   └── UserManagementController.java # REST endpoints for user management
  ├── dto/
  │   └── UserCreationRequest.java   # DTO for user creation requests
  ├── util/
  │   ├── UserConverter.java         # Utility for user-related conversions
  │   └── UserCreator.java           # Fluent interface for creating users
  └── README.md                      # Documentation
```

## User Types and Roles Hierarchy

The system implements a clear hierarchical structure:

1. **Groups**: Collections of roles
   - SuperAdmins group
   - Administrators group
   - DemoUsers group

2. **Roles**: Collections of permissions
   - SUPER_ADMIN role (highest precedence)
   - ADMIN role
   - USER role (for demo users)

3. **Permissions**: Fine-grained access controls
   - Various permissions for different resources and operations

## User Types

### 1. Super Admin
- Full system access
- Belongs to SuperAdmins group
- Has SUPER_ADMIN role
- Can manage all users, roles, and system settings
- Default credentials: superadmin@pharmacyhub.com / superadmin123

### 2. Admin
- Administrative access (but not system settings)
- Belongs to Administrators group
- Has ADMIN role
- Can manage regular operations but not system settings
- Default credentials: admin@pharmacyhub.com / admin123

### 3. Demo User
- Limited read-only access
- Belongs to DemoUsers group
- Has USER role
- For demonstration and testing purposes
- Default credentials: demo@pharmacyhub.com / demo123

## How to Use

### Creating Users Programmatically

```java
// Method 1: Using UserTypeService
@Autowired
private UserTypeService userTypeService;

// Create a super admin
User superAdmin = userTypeService.createSuperAdmin(
    "super.admin@example.com", 
    "Super", 
    "Admin", 
    "password123", 
    "1234567890"
);

// Create a regular admin
User admin = userTypeService.createAdmin(
    "admin@example.com", 
    "Regular", 
    "Admin", 
    "password123", 
    null
);

// Create a demo user
User demoUser = userTypeService.createDemoUser(
    "demo@example.com", 
    "Demo", 
    "User", 
    "password123", 
    null
);

// Method 2: Using fluent UserCreator (recommended)
@Autowired
private UserCreator userCreator;

// Create a super admin with fluent interface
User superAdmin = userCreator.superAdmin()
    .withEmail("super.admin@example.com")
    .withFirstName("Super")
    .withLastName("Admin")
    .withPassword("password123")
    .withContactNumber("1234567890")
    .build();

// Create a regular admin with fluent interface
User admin = userCreator.admin()
    .withEmail("admin@example.com")
    .withFirstName("Regular")
    .withLastName("Admin")
    .withPassword("password123")
    .build();

// Create a demo user with fluent interface
User demoUser = userCreator.demoUser()
    .withEmail("demo@example.com")
    .withFirstName("Demo")
    .withLastName("User")
    .withPassword("password123")
    .build();
```

### REST API Endpoints

The system provides REST endpoints for managing user types:

1. **Get Users**
   - GET `/api/users/super-admins` - Get all super admins
   - GET `/api/users/admins` - Get all admins
   - GET `/api/users/demo-users` - Get all demo users
   - GET `/api/users/by-type/{userType}` - Get users by type with pagination

2. **Create Users**
   - POST `/api/users/super-admins` - Create a super admin
   - POST `/api/users/admins` - Create an admin
   - POST `/api/users/demo-users` - Create a demo user

3. **User Information**
   - GET `/api/users/{userId}/permissions` - Get user permissions
   - GET `/api/users/{userId}/roles` - Get user roles
   - GET `/api/users/{userId}/is-super-admin` - Check if user is a super admin
   - GET `/api/users/{userId}/is-admin` - Check if user is an admin
   - GET `/api/users/{userId}/is-demo-user` - Check if user is a demo user

## Configuration

Default user credentials can be configured in `application.properties` or `application.yml`:

```properties
# Super Admin credentials
pharmacyhub.superadmin.email=superadmin@pharmacyhub.com
pharmacyhub.superadmin.password=superadmin123

# Admin credentials
pharmacyhub.admin.email=admin@pharmacyhub.com
pharmacyhub.admin.password=admin123

# Demo user credentials
pharmacyhub.demo.email=demo@pharmacyhub.com
pharmacyhub.demo.password=demo123
```

## Security and Authorization

The implementation includes proper security checks:

- Role-based access control using Spring Security
- Method-level security using `@PreAuthorize`
- Proper validation for user inputs
- Secure password storage using encryption