package com.pharmacyhub.security.initializer;

import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.security.domain.Feature;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.infrastructure.FeatureRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Initializes system features for role-based access control
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 5) // Run after permission initializers but before others
public class FeatureInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final FeatureRepository featureRepository;
    private final PermissionRepository permissionRepository;
    
    private boolean initialized = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized) {
            return;
        }
        
        try {
            log.info("Initializing system features");
            initializeFeatures();
            initialized = true;
            log.info("Feature initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing features: ", e);
        }
    }
    
    /**
     * Initialize system features
     */
    private void initializeFeatures() {
        // Initialize exams feature
        initializeExamsFeature();
        
        // Add other features here as needed
    }
    
    /**
     * Initialize the exams feature
     */
    private void initializeExamsFeature() {
        final String EXAMS_FEATURE = "exams";
        
        // Check if feature already exists
        Optional<Feature> existingFeature = featureRepository.findByCode(EXAMS_FEATURE);
        
        if (existingFeature.isPresent()) {
            log.debug("Exams feature already exists, updating operations");
            Feature feature = existingFeature.get();
            
            // Ensure all operations are added
            feature.getOperations().addAll(Arrays.asList(
                "VIEW", "TAKE", "CREATE", "EDIT", "DELETE", "DUPLICATE",
                "MANAGE_QUESTIONS", "PUBLISH", "UNPUBLISH", "ASSIGN",
                "GRADE", "VIEW_RESULTS", "EXPORT_RESULTS", "VIEW_ANALYTICS"
            ));
            
            // Find and add all exam permissions
            Set<Permission> permissions = new HashSet<>();
            
            for (String permName : ExamPermissionConstants.ADMIN_PERMISSIONS) {
                permissionRepository.findByName(permName).ifPresent(permissions::add);
            }
            
            feature.getPermissions().addAll(permissions);
            featureRepository.save(feature);
            log.info("Updated exams feature with {} operations and {} permissions", 
                feature.getOperations().size(), feature.getPermissions().size());
        } else {
            log.info("Creating exams feature");
            
            // Create new feature
            Feature examsFeature = Feature.builder()
                .code(EXAMS_FEATURE)
                .name("Exams Management")
                .description("Manage exams, questions, and assessments")
                .active(true)
                .operations(new HashSet<>(Arrays.asList(
                    "VIEW", "TAKE", "CREATE", "EDIT", "DELETE", "DUPLICATE",
                    "MANAGE_QUESTIONS", "PUBLISH", "UNPUBLISH", "ASSIGN",
                    "GRADE", "VIEW_RESULTS", "EXPORT_RESULTS", "VIEW_ANALYTICS"
                )))
                .build();
            
            // Find and add all exam permissions
            Set<Permission> permissions = new HashSet<>();
            
            for (String permName : ExamPermissionConstants.ADMIN_PERMISSIONS) {
                permissionRepository.findByName(permName).ifPresent(permissions::add);
            }
            
            examsFeature.setPermissions(permissions);
            featureRepository.save(examsFeature);
            log.info("Created exams feature with {} operations and {} permissions", 
                examsFeature.getOperations().size(), examsFeature.getPermissions().size());
        }
    }
}
