package com.pharmacyhub.util;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.Proprietor;
import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.domain.OperationType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for creating test data
 */
public class TestDataBuilder {

    public static User createUser(String email, String password, UserType userType) {
        // Create user without roles (roles should be added separately using TestDatabaseSetup)
        User user = User.builder()
                .emailAddress(email != null ? email : "test@pharmacyhub.pk")
                .password(password != null ? password : "password") // Should be encoded in service tests
                .firstName("Test")
                .lastName("User")
                .userType(userType != null ? userType : UserType.PHARMACIST)
                .registered(true)
                .verified(true)
                .tokenCreationDate(LocalDateTime.now())
                .active(true)
                .roles(new HashSet<>())
                .groups(new HashSet<>())
                .permissionOverrides(new HashSet<>())
                .accountNonLocked(true)
                .build();
        
        return user;
    }
    
    public static User createUserWithRoles(String email, String password, UserType userType, Set<Role> roles) {
        User user = createUser(email, password, userType);
        user.setRoles(roles != null ? roles : new HashSet<>());
        return user;
    }
    
    public static Pharmacist createPharmacist(User user) {
        if (user == null) {
            user = createUser("pharmacist@pharmacyhub.pk", "password", UserType.PHARMACIST);
        }
        
        return Pharmacist.builder()
                .categoryAvailable("Yes")
                .licenseDuration("1 year")
                .experience("2 years")
                .city("Lahore")
                .location("NFC")
                .universityName("UCP")
                .batch("F16")
                .contactNumber("03456142607")
                .user(user)
                .build();
    }
    
    public static Proprietor createProprietor(User user) {
        if (user == null) {
            user = createUser("proprietor@pharmacyhub.pk", "password", UserType.PROPRIETOR);
        }
        
        return Proprietor.builder()
                .categoryRequired("Yes")
                .licenseDuration("1 year")
                .experienced("Yes")
                .pharmacyName("Test Pharmacy")
                .city("Lahore")
                .location("NFC")
                .contactNumber("03456142607")
                .user(user)
                .build();
    }
    
    public static PharmacyManager createPharmacyManager(User user) {
        if (user == null) {
            user = createUser("manager@pharmacyhub.pk", "password", UserType.PHARMACY_MANAGER);
        }
        
        return PharmacyManager.builder()
                .contactNumber("03456142607")
                .area("NFC")
                .city("Lahore")
                .experience("2 years")
                .previousPharmacyName("ABC Pharmacy")
                .currentJobStatus("Active")
                .shiftTime("Morning")
                .user(user)
                .build();
    }
    
    public static Salesman createSalesman(User user) {
        if (user == null) {
            user = createUser("salesman@pharmacyhub.pk", "password", UserType.SALESMAN);
        }
        
        return Salesman.builder()
                .contactNumber("03456142607")
                .area("NFC")
                .city("Lahore")
                .experience("2 years")
                .previousPharmacyName("ABC Pharmacy")
                .currentJobStatus("Active")
                .shiftTime("Morning")
                .user(user)
                .build();
    }
    
    /**
     * This method is kept for backward compatibility but should be avoided in tests.
     * Use TestDatabaseSetup.getOrCreateRole instead.
     */
    public static Role createRole(RoleEnum name, int precedence) {
        if (name == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        
        Set<Permission> permissions = new HashSet<>();
        
        // Create basic permissions for this role
        Permission updateStatusPermission = createPermission(
            "UPDATE_STATUS", 
            "Can update user status",
            ResourceType.USER,
            OperationType.UPDATE);
        
        Permission viewProfilePermission = createPermission(
            "VIEW_PROFILE", 
            "Can view profile",
            ResourceType.USER,
            OperationType.READ);
            
        permissions.add(updateStatusPermission);
        permissions.add(viewProfilePermission);

        return Role.builder()
                .name(name)
                .precedence(precedence)
                .description("Test role for " + name.toString())
                .permissions(permissions)
                .childRoles(new HashSet<>())
                .system(true)
                .build();
    }
    
    public static Permission createPermission(String name, String description) {
        return createPermission(name, description, ResourceType.USER, OperationType.READ);
    }
    
    public static Permission createPermission(String name, String description, 
                                     ResourceType resourceType, OperationType operationType) {
        return Permission.builder()
                .name(name != null ? name : "DEFAULT_PERMISSION")
                .description(description != null ? description : "Default permission description")
                .resourceType(resourceType != null ? resourceType : ResourceType.USER)
                .operationType(operationType != null ? operationType : OperationType.READ)
                .requiresApproval(false)
                .build();
    }
}