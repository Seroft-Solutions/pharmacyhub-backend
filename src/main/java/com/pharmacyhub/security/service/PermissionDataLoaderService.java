package com.pharmacyhub.security.service;

import com.pharmacyhub.security.constants.AuthPermissionConstants;
import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.security.constants.PermissionConstants;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for loading and caching permission data
 * Ensures all required permissions are available in the system
 */
@Service
@RequiredArgsConstructor
public class PermissionDataLoaderService {
    private static final Logger log = LoggerFactory.getLogger(PermissionDataLoaderService.class);
    private final PermissionRepository permissionRepository;
    
    // Cache of permission definitions to avoid re-creation during initialization
    private final Map<String, PermissionDefinition> permissionDefinitions = new HashMap<>();
    
    /**
     * Initialize the permissions data loader
     * This method runs after the bean has been constructed
     */
    @PostConstruct
    @Transactional
    public void initialize() {
        log.info("Initializing Permission Data Loader Service...");
        
        // Load all permission definitions first
        loadAllPermissionDefinitions();
        
        // Synchronize repository with definitions
        synchronizePermissions();
    }
    
    /**
     * Load all permission definitions from the different modules
     */
    private void loadAllPermissionDefinitions() {
        log.info("Loading permission definitions...");
        
        // Load core permissions
        loadCorePermissions();
        
        // Load auth-specific permissions
        loadAuthPermissions();
        
        // Load exam-specific permissions
        loadExamPermissions();
        
        // Add more module-specific permissions here as needed
        
        log.info("Loaded {} permission definitions", permissionDefinitions.size());
    }
    
    /**
     * Load core permissions from PermissionConstants class
     */
    private void loadCorePermissions() {
        // User Management
        definePermission(PermissionConstants.MANAGE_USERS, 
            "Manage all users in the system", ResourceType.USER, OperationType.MANAGE);
        definePermission(PermissionConstants.VIEW_USERS, 
            "View users in the system", ResourceType.USER, OperationType.READ);
        
        // Role Management
        definePermission(PermissionConstants.MANAGE_ROLES, 
            "Manage system roles", ResourceType.ROLE, OperationType.MANAGE);
        
        // Permission Management
        definePermission(PermissionConstants.MANAGE_PERMISSIONS, 
            "Manage system permissions", ResourceType.PERMISSION, OperationType.MANAGE);
        
        // Group Management
        definePermission(PermissionConstants.MANAGE_GROUPS, 
            "Manage system groups", ResourceType.GROUP, OperationType.MANAGE);
        
        // System Management
        definePermission(PermissionConstants.MANAGE_SYSTEM, 
            "Manage system operations", ResourceType.SYSTEM_SETTING, OperationType.MANAGE);
        definePermission(PermissionConstants.MANAGE_SYSTEM_SETTINGS, 
            "Manage system settings", ResourceType.SYSTEM_SETTING, OperationType.UPDATE);
        
        // Audit Management
        definePermission(PermissionConstants.VIEW_AUDIT_LOGS, 
            "View all audit logs", ResourceType.AUDIT_LOG, OperationType.READ);
        definePermission(PermissionConstants.VIEW_OWN_AUDIT_LOGS, 
            "View own audit logs", ResourceType.AUDIT_LOG, OperationType.VIEW_OWN);
        definePermission(PermissionConstants.EXPORT_AUDIT_LOGS, 
            "Export audit logs", ResourceType.AUDIT_LOG, OperationType.EXPORT);
        
        // Inventory Management
        definePermission(PermissionConstants.MANAGE_INVENTORY, 
            "Manage inventory", ResourceType.INVENTORY, OperationType.MANAGE);
        definePermission(PermissionConstants.VIEW_PRODUCTS, 
            "View products", ResourceType.MEDICINE, OperationType.READ);
        
        // Reports
        definePermission(PermissionConstants.VIEW_REPORTS, 
            "View reports", ResourceType.REPORTS, OperationType.READ);
    }
    
    /**
     * Load auth-specific permissions from AuthPermissionConstants class
     */
    private void loadAuthPermissions() {
        // Authentication
        definePermission(AuthPermissionConstants.LOGIN, 
            "Log into the system", ResourceType.USER, OperationType.READ);
        definePermission(AuthPermissionConstants.LOGOUT, 
            "Log out of the system", ResourceType.USER, OperationType.READ);
        definePermission(AuthPermissionConstants.REGISTER, 
            "Register a new account", ResourceType.USER, OperationType.CREATE);
        
        // Account Management
        definePermission(AuthPermissionConstants.MANAGE_ACCOUNT, 
            "Manage own account settings", ResourceType.USER, OperationType.UPDATE);
        definePermission(AuthPermissionConstants.VERIFY_EMAIL, 
            "Verify email address", ResourceType.USER, OperationType.UPDATE);
        definePermission(AuthPermissionConstants.RESET_PASSWORD, 
            "Reset password", ResourceType.USER, OperationType.UPDATE);
        
        // Profile
        definePermission(AuthPermissionConstants.VIEW_PROFILE, 
            "View own profile", ResourceType.USER, OperationType.READ);
        definePermission(AuthPermissionConstants.EDIT_PROFILE, 
            "Edit own profile", ResourceType.USER, OperationType.UPDATE);
        
        // Sessions
        definePermission(AuthPermissionConstants.MANAGE_SESSIONS, 
            "Manage active sessions", ResourceType.USER, OperationType.MANAGE);
        definePermission(AuthPermissionConstants.VIEW_SESSIONS, 
            "View active sessions", ResourceType.USER, OperationType.READ);
        
        // User Management (Admin)
        definePermission(AuthPermissionConstants.MANAGE_USERS, 
            "Manage all users", ResourceType.USER, OperationType.MANAGE);
        definePermission(AuthPermissionConstants.VIEW_USERS, 
            "View all users", ResourceType.USER, OperationType.READ);
        definePermission(AuthPermissionConstants.EDIT_USERS, 
            "Edit user details", ResourceType.USER, OperationType.UPDATE);
        definePermission(AuthPermissionConstants.DELETE_USERS, 
            "Delete users", ResourceType.USER, OperationType.DELETE);
        definePermission(AuthPermissionConstants.IMPERSONATE_USER, 
            "Impersonate other users", ResourceType.USER, OperationType.MANAGE, true); // Requires approval
    }
    
    /**
     * Load exam-specific permissions from ExamPermissionConstants class
     */
    private void loadExamPermissions() {
        // Basic Exam Access
        definePermission(ExamPermissionConstants.VIEW_EXAMS, 
            "View available exams", ResourceType.PHARMACY, OperationType.READ);
        definePermission(ExamPermissionConstants.TAKE_EXAM, 
            "Take exams", ResourceType.PHARMACY, OperationType.UPDATE);
        
        // Exam Creation & Management
        definePermission(ExamPermissionConstants.CREATE_EXAM, 
            "Create new exams", ResourceType.PHARMACY, OperationType.CREATE);
        definePermission(ExamPermissionConstants.EDIT_EXAM, 
            "Edit existing exams", ResourceType.PHARMACY, OperationType.UPDATE);
        definePermission(ExamPermissionConstants.DELETE_EXAM, 
            "Delete exams", ResourceType.PHARMACY, OperationType.DELETE);
        definePermission(ExamPermissionConstants.DUPLICATE_EXAM, 
            "Duplicate exams", ResourceType.PHARMACY, OperationType.CREATE);
        
        // Question Management
        definePermission(ExamPermissionConstants.MANAGE_QUESTIONS, 
            "Manage questions within exams", ResourceType.PHARMACY, OperationType.MANAGE);
        
        // Exam Administration
        definePermission(ExamPermissionConstants.PUBLISH_EXAM, 
            "Publish exams to make them available", ResourceType.PHARMACY, OperationType.UPDATE);
        definePermission(ExamPermissionConstants.UNPUBLISH_EXAM, 
            "Unpublish exams", ResourceType.PHARMACY, OperationType.UPDATE);
        definePermission(ExamPermissionConstants.ASSIGN_EXAM, 
            "Assign exams to users", ResourceType.PHARMACY, OperationType.UPDATE);
        
        // Results & Grading
        definePermission(ExamPermissionConstants.GRADE_EXAM, 
            "Grade exam attempts", ResourceType.PHARMACY, OperationType.UPDATE);
        definePermission(ExamPermissionConstants.VIEW_RESULTS, 
            "View exam results", ResourceType.PHARMACY, OperationType.READ);
        definePermission(ExamPermissionConstants.EXPORT_RESULTS, 
            "Export exam results", ResourceType.REPORTS, OperationType.EXPORT);
        
        // Analytics
        definePermission(ExamPermissionConstants.VIEW_ANALYTICS, 
            "View exam analytics", ResourceType.ANALYTICS, OperationType.READ);
    }
    
    /**
     * Define a permission with the given attributes
     */
    private void definePermission(String name, String description, 
                                 ResourceType resourceType, OperationType operationType,
                                 boolean requiresApproval) {
        permissionDefinitions.put(name, new PermissionDefinition(
            name, description, resourceType, operationType, requiresApproval
        ));
    }
    
    /**
     * Define a permission with default requiresApproval = false
     */
    private void definePermission(String name, String description, 
                                 ResourceType resourceType, OperationType operationType) {
        definePermission(name, description, resourceType, operationType, false);
    }
    
    /**
     * Synchronize permissions in the database with the defined permissions
     */
    @Transactional
    public void synchronizePermissions() {
        log.info("Synchronizing permissions with database...");
        
        // Get all existing permissions from the database
        List<Permission> existingPermissions = permissionRepository.findAll();
        Map<String, Permission> existingPermissionMap = existingPermissions.stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));
        
        int created = 0;
        int updated = 0;
        
        // Synchronize each defined permission
        for (PermissionDefinition definition : permissionDefinitions.values()) {
            Permission permission = existingPermissionMap.get(definition.getName());
            
            if (permission == null) {
                // Create new permission
                permission = new Permission();
                permission.setName(definition.getName());
                permission.setDescription(definition.getDescription());
                permission.setResourceType(definition.getResourceType());
                permission.setOperationType(definition.getOperationType());
                permission.setRequiresApproval(definition.isRequiresApproval());
                permissionRepository.save(permission);
                created++;
            } else {
                // Update existing permission if needed
                boolean needsUpdate = false;
                
                if (!Objects.equals(permission.getDescription(), definition.getDescription())) {
                    permission.setDescription(definition.getDescription());
                    needsUpdate = true;
                }
                
                if (permission.getResourceType() != definition.getResourceType()) {
                    permission.setResourceType(definition.getResourceType());
                    needsUpdate = true;
                }
                
                if (permission.getOperationType() != definition.getOperationType()) {
                    permission.setOperationType(definition.getOperationType());
                    needsUpdate = true;
                }
                
                if (permission.isRequiresApproval() != definition.isRequiresApproval()) {
                    permission.setRequiresApproval(definition.isRequiresApproval());
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    permissionRepository.save(permission);
                    updated++;
                }
            }
        }
        
        log.info("Permission synchronization complete. Created: {}, Updated: {}", created, updated);
    }
    
    /**
     * Get all permission data in a structured format
     * This is primarily used for the frontend to display all available permissions
     */
    @Cacheable("permissionStructure")
    public Map<String, List<Map<String, Object>>> getPermissionStructure() {
        log.debug("Building permission structure...");
        Map<String, List<Map<String, Object>>> structure = new HashMap<>();
        
        // Group permissions by resource type
        List<Permission> permissions = permissionRepository.findAll();
        Map<ResourceType, List<Permission>> permissionsByResource = permissions.stream()
                .collect(Collectors.groupingBy(Permission::getResourceType));
        
        // Convert to structured format
        for (Map.Entry<ResourceType, List<Permission>> entry : permissionsByResource.entrySet()) {
            String resourceType = entry.getKey().name();
            List<Map<String, Object>> permList = new ArrayList<>();
            
            for (Permission perm : entry.getValue()) {
                Map<String, Object> permData = new HashMap<>();
                permData.put("name", perm.getName());
                permData.put("description", perm.getDescription());
                permData.put("operation", perm.getOperationType().name());
                permData.put("requiresApproval", perm.isRequiresApproval());
                
                permList.add(permData);
            }
            
            structure.put(resourceType, permList);
        }
        
        return structure;
    }
    
    /**
     * Get permissions by resource type
     */
    @Cacheable("permissionsByResource")
    public List<Permission> getPermissionsByResourceType(ResourceType resourceType) {
        return permissionRepository.findAll().stream()
                .filter(p -> p.getResourceType() == resourceType)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all permissions
     */
    @Cacheable("allPermissions")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    
    /**
     * Get permissions for a specific feature
     */
    @Cacheable("featurePermissions")
    public List<Permission> getFeaturePermissions(String feature) {
        String prefix = feature + ":";
        return permissionRepository.findAll().stream()
                .filter(p -> p.getName().startsWith(prefix))
                .collect(Collectors.toList());
    }
    
    /**
     * Get permissions for the exams feature
     */
    public List<Permission> getExamPermissions() {
        return getFeaturePermissions("exams");
    }
    
    /**
     * Get permissions for the auth feature
     */
    public List<Permission> getAuthPermissions() {
        return getFeaturePermissions("auth");
    }
    
    /**
     * PermissionDefinition - Internal class for defining permissions
     */
    private static class PermissionDefinition {
        private final String name;
        private final String description;
        private final ResourceType resourceType;
        private final OperationType operationType;
        private final boolean requiresApproval;
        
        public PermissionDefinition(String name, String description, 
                                   ResourceType resourceType, OperationType operationType,
                                   boolean requiresApproval) {
            this.name = name;
            this.description = description;
            this.resourceType = resourceType;
            this.operationType = operationType;
            this.requiresApproval = requiresApproval;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public ResourceType getResourceType() {
            return resourceType;
        }
        
        public OperationType getOperationType() {
            return operationType;
        }
        
        public boolean isRequiresApproval() {
            return requiresApproval;
        }
    }
}
