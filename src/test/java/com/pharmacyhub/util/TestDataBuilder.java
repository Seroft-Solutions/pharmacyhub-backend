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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for creating test data
 */
public class TestDataBuilder {

    public static User createUser(String email, String password, UserType userType) {
        Set<Role> roles = new HashSet<>();
        Role userRole = createRole(RoleEnum.USER, 1);
        Permission viewProfilePermission = createPermission("VIEW_PROFILE", "Can view profile");
        Set<Permission> permissions = new HashSet<>();
        permissions.add(viewProfilePermission);
        userRole.setPermissions(permissions);
        roles.add(userRole);
        User user = User.builder()
                .emailAddress(email)
                .password(password) // Should be encoded in service tests
                .firstName("Test")
                .lastName("User")
                .userType(userType)
                .registered(true)
                .verified(true)
                .tokenCreationDate(LocalDateTime.now())
                .active(true)
                .roles(roles)
                .accountNonLocked(true)
                .build();
        
        return user;
    }
    
    public static Pharmacist createPharmacist(User user) {
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
    
    public static Role createRole(RoleEnum name, int precedence) {
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
                .description("Test role")
                .permissions(permissions)
                .childRoles(new HashSet<>())
                .system(true)
                .build();
    }
    
    public static Permission createPermission(String name, String description) {
        return Permission.builder()
                .name(name)
                .description(description)
                .requiresApproval(false)
                .build();
    }
    
    public static Permission createPermission(String name, String description, 
                                     ResourceType resourceType, OperationType operationType) {
        return Permission.builder()
                .name(name)
                .description(description)
                .resourceType(resourceType)
                .operationType(operationType)
                .requiresApproval(false)
                .build();
    }
}