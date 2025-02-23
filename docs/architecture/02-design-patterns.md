# Design Patterns

This document outlines the key design patterns used in the PharmacyHub backend application.

## Core Patterns

### 1. Base Engine Pattern

The Base Engine Pattern is implemented through `PHEngine` and provides common functionality across services.

```java
@Component
public class PHEngine {
    public User getLoggedInUser() {
        // Implementation
    }
    
    // Other common methods
}

@Service
public class UserService extends PHEngine {
    // Service implementation using base functionality
}
```

**When to use:**
- When implementing new services that need access to common functionality
- When adding features that require user context
- For cross-cutting concerns

### 2. Mapper Pattern

Centralized mapping through `PHMapper` using ModelMapper.

```java
@Component
public class PHMapper {
    private ModelMapper modelMapper = new ModelMapper();
    
    // Entity to DTO
    public UserDTO getUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
    
    // DTO to Entity
    public User getUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}
```

**When to use:**
- When adding new DTOs
- When implementing new entity mappings
- For consistent object transformation

### 3. Repository Pattern

Interface-based repositories using Spring Data JPA.

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAddress(String emailAddress);
    List<User> findByUserType(String userType);
}
```

**When to use:**
- When adding new entities
- When implementing custom queries
- For data access operations

### 4. Service Pattern

Interface-based services with clear business logic separation.

```java
public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(UserDTO userDTO);
    void deleteUser(Long id);
}

@Service
public class UserServiceImpl implements UserService {
    // Implementation
}
```

**When to use:**
- When adding new business functionality
- When implementing business rules
- For transaction management

## Implementation Guidelines

### 1. SOLID Principles

#### Single Responsibility
```java
// Good
public class UserService {
    public User createUser() { }
    public User updateUser() { }
}

// Bad
public class UserService {
    public User createUser() { }
    public void sendEmail() { }
}
```

#### Open/Closed
```java
// Good
public interface UserValidator {
    boolean validate(User user);
}

public class EmailValidator implements UserValidator {
    public boolean validate(User user) { }
}
```

#### Liskov Substitution
```java
// Good
public class Pharmacist extends User {
    @Override
    public void updateProfile() {
        // Specific implementation
    }
}
```

#### Interface Segregation
```java
// Good
public interface UserReader {
    User getUser(Long id);
    List<User> getAllUsers();
}

public interface UserWriter {
    User createUser(User user);
    User updateUser(User user);
}
```

#### Dependency Inversion
```java
// Good
@Service
public class UserService {
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### 2. Pattern Usage Guidelines

1. **When Creating New Features**
   - Identify required patterns
   - Follow existing implementations
   - Maintain consistency

2. **When Extending Functionality**
   - Use appropriate patterns
   - Maintain separation of concerns
   - Follow SOLID principles

3. **When Implementing Business Logic**
   - Use service pattern
   - Implement proper validation
   - Handle errors appropriately

## Common Anti-patterns to Avoid

1. **God Objects**
```java
// Bad
public class UserService {
    public User createUser() { }
    public void sendEmail() { }
    public void generateReport() { }
    public void processPayment() { }
}
```

2. **Anemic Domain Model**
```java
// Bad
public class User {
    private String name;
    private String email;
    // Only getters and setters
}
```

3. **Mixed Responsibilities**
```java
// Bad
public class UserController {
    public User createUser() {
        // Direct database operations
        // Email sending
        // PDF generation
    }
}
```

## Best Practices

1. **Pattern Selection**
   - Choose appropriate patterns
   - Consider maintainability
   - Follow existing patterns

2. **Implementation**
   - Follow SOLID principles
   - Maintain clean code
   - Write proper documentation

3. **Testing**
   - Test pattern implementations
   - Verify pattern behavior
   - Ensure pattern consistency