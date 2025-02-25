# PharmacyHub Backend Project Summary

This document summarizes the architecture, functionality, and key features of the PharmacyHub backend project, based on the code analysis performed.

## 1. Architecture

*   **Layered Clean Architecture:** The project follows a layered architecture, typical for Spring Boot applications, separating concerns into distinct layers:
    *   **Controller Layer:**  Handles HTTP requests and API endpoints (e.g., `AuthController`, `UserController`, `RBACController`, `ExamController`).
    *   **Service Layer:** Contains business logic and orchestrates operations (e.g., `UserService`, `RBACService`, `ExamService`, `OtpService`).
    *   **Repository Layer:**  Manages data persistence and database interactions using JPA/Hibernate (e.g., `UserRepository`, `RoleRepository`, `PermissionRepository`).
    *   **Entity Layer:** Defines domain entities representing data models (e.g., `User`, `Role`, `Permission`, `Exam`, `Group`).
    *   **DTO Layer:** Data Transfer Objects for request and response payloads, ensuring data encapsulation and API contract stability (e.g., `UserDTO`, `RoleDTO`, `ExamDTO`).
*   **Technology Stack:**
    *   **Backend Framework:** Spring Boot
    *   **Database:** PostgreSQL
    *   **Authentication:** JWT (JSON Web Tokens)
    *   **Authorization:** RBAC (Role-Based Access Control) with Permissions, Roles, Groups, and Role Hierarchy
    *   **Caching:** Spring Cache Abstraction (likely using in-memory or Redis)
    *   **Email:** JavaMail API (configured for Gmail SMTP)

## 2. Key Functionalities

*   **User Management:**
    *   Supports multiple user roles: Admin, Pharmacist, Pharmacy Manager, Proprietor, Salesman.
    *   User registration, email verification, login, and password management.
    *   User connection management (PharmacistConnections, etc.).
    *   Role and group assignment to users.
    *   Comprehensive RBAC implementation with Permissions, Roles, Groups, and Role Hierarchy.
*   **Authentication and Authorization:**
    *   **JWT Authentication:** Secure, stateless authentication using JWT.
    *   **RBAC Authorization:** Fine-grained access control based on roles, permissions, groups, and permission overrides.
    *   Method-level security using `@PreAuthorize` and custom permission evaluation (`PHPermissionEvaluator`, `PermissionAspect`).
*   **Exam Management:**
    *   CRUD operations for Exams.
    *   Exam publishing and archiving.
    *   Retrieval of exams by status.
*   **Data Entry:**
    *   Entry management with CRUD operations and search functionality.
    *   Integration with Google Contacts API to save entry data.
*   **OTP (One-Time Password) Service:**
    *   Generation and validation of OTPs for password reset and potentially other security-sensitive operations.
*   **Email Service:**
    *   Sending emails for user verification, OTPs, and potentially notifications.
    *   Uses HTML templates for email content.
*   **Audit Logging:**
    *   Logs security-related events, such as role creation, permission changes, and access validation.

## 3. Security Features

*   **JWT Authentication:** Ensures secure API access using JSON Web Tokens.
*   **Role-Based Access Control (RBAC):** Implements fine-grained authorization to control user access to resources and operations based on their roles, groups, and permissions. Includes role hierarchy and permission overrides.
*   **Spring Security:** Leverages Spring Security framework for comprehensive security management.
*   **Input Validation:**  Likely implemented to prevent common security vulnerabilities (though not explicitly reviewed in detail).
*   **Password Encoding:** Uses `PasswordEncoder` (likely BCrypt) for secure password storage.
*   **CORS Configuration:** Configured for Cross-Origin Resource Sharing (currently permissive, needs review for production).

## 4. Development and Testing

*   **Test Configuration:** Includes test configurations and integration tests.
*   **Database Seeding:**  Potentially includes seeder engines for initial data population (AdminUserSeeder, RoleSeeder, etc.).
*   **Development Profile:** `application.yml` suggests a development environment (`ddl-auto: create-drop`, `show-sql: true`).

## 5. Potential Areas for Further Investigation

*   **Frontend Integration:**  The project is likely intended to be integrated with a frontend application (possibly Next.js as per user instructions), but the frontend codebase was not analyzed.
*   **API Documentation:**  Swagger/OpenAPI documentation is mentioned in user instructions but not explicitly found in the analyzed files.
*   **Performance Optimization:** While caching is implemented, further performance optimizations (e.g., database query optimization, connection pooling) might be beneficial.
*   **Production Readiness:**  Configuration settings like `ddl-auto`, `show-sql`, and CORS need to be reviewed and adjusted for a production environment.
*   **Detailed Security Audit:** A comprehensive security audit is recommended to identify and address potential vulnerabilities.
*   **RBAC Implementation Details:** Further investigation into `RBACService`, `PHPermissionEvaluator`, `PermissionAspect`, `RoleHierarchyInitializer`, and `RoleInitializer` would provide deeper insights into the RBAC implementation.

This summary provides a high-level overview of the PharmacyHub backend project. Further in-depth analysis of specific modules or features can be performed as needed.