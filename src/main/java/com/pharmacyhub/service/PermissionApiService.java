package com.pharmacyhub.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.service.RBACService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling permission-related API operations
 * Provides methods for the API controllers to check user permissions
 */
@Service
public class PermissionApiService {
    private static final Logger logger = LoggerFactory.getLogger(PermissionApiService.class);
    
    private final RBACService rbacService;
    private final UserService userService;
    
    // Cache for permission checks to reduce DB queries
    private final Map<String, Boolean> permissionCache = new ConcurrentHashMap<>();
    
    public PermissionApiService(RBACService rbacService, UserService userService) {
        this.rbacService = rbacService;
        this.userService = userService;
    }
    
    /**
     * Check if the current user has a specific permission
     */
    public boolean hasPermission(String permission) {
        User currentUser = userService.getLoggedInUser();
        if (currentUser == null) {
            return false;
        }
        
        // Generate cache key
        String cacheKey = currentUser.getId() + ":" + permission;
        
        // Check cache first
        Boolean cachedResult = permissionCache.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // Get permissions and check
        boolean hasPermission = rbacService.userHasPermission(currentUser.getId(), permission);
        
        // Cache the result
        permissionCache.put(cacheKey, hasPermission);
        
        return hasPermission;
    }
    
    /**
     * Check multiple permissions at once for the current user
     * Returns a map of permission names to boolean values
     */
    public Map<String, Boolean> checkMultiplePermissions(String... permissions) {
        User currentUser = userService.getLoggedInUser();
        Map<String, Boolean> result = new HashMap<>();
        
        if (currentUser == null) {
            for (String permission : permissions) {
                result.put(permission, false);
            }
            return result;
        }
        
        Set<Permission> userPermissions = rbacService.getUserEffectivePermissions(currentUser.getId());
        
        for (String permission : permissions) {
            boolean hasPermission = userPermissions.stream()
                    .anyMatch(p -> p.getName().equals(permission));
            
            result.put(permission, hasPermission);
        }
        
        return result;
    }
    
    /**
     * Check all exam permissions for the current user
     * Returns a map of exam permission names to boolean values
     */
    public Map<String, Boolean> checkExamPermissions() {
        Map<String, Boolean> result = new HashMap<>();
        
        // Basic exam access
        result.put(ExamPermissionConstants.VIEW_EXAMS, hasPermission(ExamPermissionConstants.VIEW_EXAMS));
        result.put(ExamPermissionConstants.TAKE_EXAM, hasPermission(ExamPermissionConstants.TAKE_EXAM));
        
        // Exam management
        result.put(ExamPermissionConstants.CREATE_EXAM, hasPermission(ExamPermissionConstants.CREATE_EXAM));
        result.put(ExamPermissionConstants.EDIT_EXAM, hasPermission(ExamPermissionConstants.EDIT_EXAM));
        result.put(ExamPermissionConstants.DELETE_EXAM, hasPermission(ExamPermissionConstants.DELETE_EXAM));
        result.put(ExamPermissionConstants.DUPLICATE_EXAM, hasPermission(ExamPermissionConstants.DUPLICATE_EXAM));
        
        // Question management
        result.put(ExamPermissionConstants.MANAGE_QUESTIONS, hasPermission(ExamPermissionConstants.MANAGE_QUESTIONS));
        
        // Exam administration
        result.put(ExamPermissionConstants.PUBLISH_EXAM, hasPermission(ExamPermissionConstants.PUBLISH_EXAM));
        result.put(ExamPermissionConstants.UNPUBLISH_EXAM, hasPermission(ExamPermissionConstants.UNPUBLISH_EXAM));
        result.put(ExamPermissionConstants.ASSIGN_EXAM, hasPermission(ExamPermissionConstants.ASSIGN_EXAM));
        
        // Results & grading
        result.put(ExamPermissionConstants.GRADE_EXAM, hasPermission(ExamPermissionConstants.GRADE_EXAM));
        result.put(ExamPermissionConstants.VIEW_RESULTS, hasPermission(ExamPermissionConstants.VIEW_RESULTS));
        result.put(ExamPermissionConstants.EXPORT_RESULTS, hasPermission(ExamPermissionConstants.EXPORT_RESULTS));
        
        // Analytics
        result.put(ExamPermissionConstants.VIEW_ANALYTICS, hasPermission(ExamPermissionConstants.VIEW_ANALYTICS));
        
        return result;
    }
    
    /**
     * Check if the current user has admin privileges for exams
     * Admin privileges include create, edit, delete, publish, etc.
     */
    public boolean hasExamAdminPrivileges() {
        return hasPermission(ExamPermissionConstants.CREATE_EXAM) &&
               hasPermission(ExamPermissionConstants.EDIT_EXAM) &&
               hasPermission(ExamPermissionConstants.PUBLISH_EXAM);
    }
    
    /**
     * Clear the permission cache for a user
     * Should be called when a user's permissions change
     */
    public void clearPermissionCache(Long userId) {
        if (userId == null) {
            permissionCache.clear();
            return;
        }
        
        String userPrefix = userId + ":";
        permissionCache.keySet().removeIf(key -> key.startsWith(userPrefix));
    }
}
