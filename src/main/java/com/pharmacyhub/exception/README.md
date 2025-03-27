# Exception Handling Framework

This package provides a centralized and standardized approach to exception handling for the PharmacyHub application.

## Components

### Core Components

- `ExceptionConstants`: Enumeration of standardized exception constants with error codes, HTTP status, messages, and resolution instructions.
- `BaseException`: Abstract base class for all application exceptions with standardized fields for error code, message, resolution, and HTTP status.
- `ExceptionHandlerService`: Centralized service for handling different types of exceptions.

### Global Exception Handlers

- `GlobalExceptionHandler`: REST controller advice for handling all application exceptions.
- `SessionExceptionHandler`: Specialized handler for session-related exceptions.

### Specialized Exceptions

- Authentication/Authorization:
  - `UnauthorizedException`: For authentication failures.
  - `ForbiddenException`: For authorization failures.

- Resource Management:
  - `ResourceNotFoundException`: For when a requested resource doesn't exist.
  - `ConflictException`: For resource conflicts (e.g., duplicates).
  - `BadRequestException`: For invalid request parameters.

- Session Management:
  - `SessionExpiredException`: For expired sessions.
  - `SessionNotFoundException`: For missing sessions.
  - `SessionValidationException`: For session validation failures.
  - `SessionConflictException`: For session conflicts (e.g., multiple logins).

### Response Wrappers

- `ApiResponse<T>`: Generic wrapper for all API responses.
- `ApiErrorResponse`: Specialized wrapper for error responses.

## Usage

### Creating a New Exception

```java
// Using ExceptionConstants
throw new UnauthorizedException(ExceptionConstants.AUTHENTICATION_FAILED);

// With custom message
throw new ResourceNotFoundException(
    ExceptionConstants.RESOURCE_NOT_FOUND,
    "User with ID " + userId + " not found."
);
```

### Handling Exceptions

The framework automatically handles exceptions and generates appropriate responses. If you need custom handling:

```java
try {
    // Your code
} catch (Exception ex) {
    exceptionHandlerService.handleException(ex, request, log);
}
```

### Error Response Structure

```json
{
  "status": 404,
  "errorCode": "ERR_NOT_FOUND",
  "message": "The requested resource was not found.",
  "resolution": "Please check the resource identifier and try again.",
  "timestamp": "2025-03-27 12:34:56",
  "path": "/api/users/123",
  "details": {
    // Additional error details if applicable
  }
}
```

## Error Codes

All error codes are defined in the `ExceptionConstants` enum. The format is generally:

- `ERR_AUTHENTICATION`: Authentication failures
- `ERR_ACCESS_DENIED`: Authorization failures
- `ERR_NOT_FOUND`: Resource not found
- `ERR_VALIDATION`: Validation errors
- `ERR_SESSION_*`: Session-related errors
- `ERR_INTERNAL`: Internal server errors

## Resolution Messages

Each error includes a resolution message to guide the user on how to resolve the issue. These are standardized in the `ExceptionConstants` enum.
