# PharmacyHub Backend Integration Test Guide

This document provides an overview of the integration tests for the PharmacyHub backend application.

## Test Structure

The integration tests are organized according to the following structure:

```
src/test/java/com/pharmacyhub/
├── config/                  # Test configuration classes
│   ├── BaseIntegrationTest.java
│   └── TestConfig.java
├── controller/              # Controller tests
│   ├── AuthControllerIntegrationTest.java
│   ├── EntryControllerIntegrationTest.java
│   ├── PharmacistControllerIntegrationTest.java
│   └── UserControllerIntegrationTest.java
├── security/                # Security-related tests
│   ├── JwtHelperTest.java
│   ├── RBACPermissionEvaluatorTest.java
│   └── service/
│       ├── RBACServiceIntegrationTest.java
│       └── RoleHierarchyServiceIntegrationTest.java
├── service/                 # Service tests
│   ├── EmailServiceTest.java
│   ├── EntryServiceIntegrationTest.java
│   ├── PharmacistServiceIntegrationTest.java
│   └── UserServiceIntegrationTest.java
└── util/                    # Test utilities
    ├── TestDataBuilder.java
    ├── TestSecurityUtils.java
    └── WithMockUserPrincipal.java
```

## Test Configuration

All tests use the following configuration:

1. H2 in-memory database for testing
2. Mockito for mocking dependencies
3. Spring Security Test for authentication and authorization testing
4. JUnit 5 as the testing framework

The test configuration is controlled by:

- `src/test/resources/application-test.yml`: Sets up the test database and other test-specific settings
- `src/test/resources/logback-test.xml`: Configures logging for tests

## Core Test Classes

### BaseIntegrationTest

All integration tests extend the `BaseIntegrationTest` class, which provides common configuration and utilities:

```java
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    // Common test utilities and setup
}
```

### TestDataBuilder

The `TestDataBuilder` class creates test data for various entities in the system:

```java
public class TestDataBuilder {
    public static User createUser(String email, String password, UserType userType) {
        // Create and return user
    }
    
    public static Pharmacist createPharmacist(User user) {
        // Create and return pharmacist
    }
    
    // Other data creation methods
}
```

### TestSecurityUtils

This utility class helps set up security contexts for tests:

```java
public class TestSecurityUtils {
    public static void setSecurityContext(User user) {
        // Set up security context with the provided user
    }
    
    public static void clearSecurityContext() {
        // Clear security context
    }
}
```

## Running Tests

### Using Maven

To run all tests:

```bash
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=PharmacistServiceIntegrationTest
```

To run a specific test method:

```bash
mvn test -Dtest=PharmacistServiceIntegrationTest#testSaveUser
```

### Using IDE

Tests can also be run directly from your IDE (IntelliJ IDEA, Eclipse, etc.) by right-clicking on the test class or method and selecting "Run Test."

## Test Coverage

The tests cover the following key areas of the application:

### Authentication and Authorization

- User registration and verification
- Login and JWT token generation
- Role-based access control (RBAC)
- Permission evaluation

### User Management

- User creation and updates
- Password management
- User profile management

### Pharmacist, Proprietor, and Other User Types

- User type-specific functionality
- Connection management between different user types

### Entry Management

- Creation and management of entries
- Search and filtering of entries

## Mocking External Services

External services are mocked to ensure tests run without external dependencies:

- Email Service: Mocked to avoid sending actual emails
- Google Contacts Service: Mocked to avoid actual API calls

## Test Reports

After running tests, you can find the test reports in:

- JUnit reports: `target/surefire-reports/`
- JaCoCo code coverage: `target/site/jacoco/`

To generate a coverage report:

```bash
mvn verify
```

Then open `target/site/jacoco/index.html` in a browser to view the code coverage report.

## Troubleshooting Common Test Issues

### Database Issues

If you encounter database-related errors:
- Ensure H2 dependency is properly included in pom.xml
- Check the application-test.yml configuration
- Verify that database migration scripts are compatible with H2

### Authentication Issues

For authentication-related test failures:
- Verify that the mocked authentication is set up correctly
- Check that the required roles and permissions are defined

### Spring Context Issues

If the Spring context fails to load:
- Check for conflicting bean definitions
- Ensure that all required beans are defined for the test context
- Use @MockBean for external dependencies

## Guidelines for Adding New Tests

When adding new functionality, follow these testing guidelines:

1. Create integration tests for all new controllers and services
2. Test both positive paths (successful operations) and negative paths (error handling)
3. Mock external dependencies to keep tests fast and reliable
4. Use the provided utility classes to create test data and security contexts
5. Keep tests focused on business functionality, not implementation details

## Security Testing Guidelines

When testing security features:

1. Test with different user roles to ensure proper authorization
2. Verify that endpoints enforce proper authentication
3. Test permission inheritance through the role hierarchy
4. Check that sensitive operations require appropriate permissions
