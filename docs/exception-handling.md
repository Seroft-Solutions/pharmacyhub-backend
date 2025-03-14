# PharmacyHub Exception Handling

## Overview

This document describes how exceptions and error responses are handled in the PharmacyHub backend application.

## Exception Handling Architecture

The application implements a global exception handling mechanism that ensures consistent error responses across all controllers. Key components include:

### 1. `GlobalExceptionHandler`

A centralized exception handler that captures and processes all exceptions thrown within the application. It maps exceptions to appropriate HTTP status codes and formats error responses consistently.

### 2. Standard Error Response Format

All error responses follow this structure:

```json
{
  "status": 400,
  "errorCode": "ERR_VALIDATION",
  "message": "Validation failed. Please check your input.",
  "timestamp": "2023-07-21T14:30:45",
  "path": "/api/resource",
  "details": {
    "field1": "Error message for field1",
    "field2": "Error message for field2"
  }
}
```

### 3. Custom Exception Types

The application uses custom exception types to represent different error scenarios:

- `BaseException`: Abstract base class for all application exceptions
- `ResourceNotFoundException`: For 404 errors
- `BadRequestException`: For 400 errors
- `ForbiddenException`: For 403 errors
- `ConflictException`: For 409 errors
- `UnauthorizedException`: For 401 errors

### 4. Security Exception Handling

Security exceptions (401, 403) are handled through:

- `CustomAuthenticationEntryPoint`: Handles unauthenticated requests
- `CustomAccessDeniedHandler`: Handles unauthorized access attempts
- Provides detailed logging for security violations

## Best Practices for Exception Handling

1. **Use Custom Exceptions**: When writing service or controller methods, use appropriate custom exceptions to signal specific error conditions.

```java
// Example
if (user == null) {
    throw new ResourceNotFoundException("User", "id", userId);
}

if (!isValidOperation(status)) {
    throw new BadRequestException("Cannot change status from " + currentStatus + " to " + newStatus);
}

if (!hasPermission(user, "EDIT_RESOURCE")) {
    throw new ForbiddenException("resource", "EDIT");
}
```

2. **Include Meaningful Error Messages**: Error messages should be clear, concise, and actionable for the client.

3. **Validate Input Early**: Use Bean Validation and validate input parameters as early as possible.

4. **Log Appropriately**: Sensitive information should not be included in client-facing error messages but should be logged for troubleshooting.

5. **Don't Expose Implementation Details**: Error responses should never expose internal implementation details, stack traces, or sensitive information.

## Handling 403 Forbidden Errors

When a 403 Forbidden error occurs, the system:

1. Logs detailed information about the request, user, and attempted action
2. Returns a standardized error response with appropriate error code
3. Captures diagnostics information for later analysis

### Common Causes of 403 Errors

- User lacks required role or permission
- JWT token is valid but doesn't have sufficient privileges
- Access policy restriction based on time, IP, or other factors
- Cross-tenant access attempt

### Troubleshooting 403 Errors

For developers/admins: Check the security logs (`logs/security.log`) which contain detailed diagnostics about:

- User identity and roles/permissions
- Requested resource and required permission
- Security context details

## Error Logging

All exceptions are logged with appropriate severity levels:

- ERROR: For 5xx errors
- WARN: For 4xx errors
- INFO: For normal operations

Security-related exceptions are logged with additional context to assist in troubleshooting.
