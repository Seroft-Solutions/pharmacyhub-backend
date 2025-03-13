package com.pharmacyhub.security.users.initializer;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.users.factory.UserTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes default users for the system.
 * Creates a super admin, regular admin, and demo user if they don't already exist.
 * Depends on GroupSeeder to ensure groups are created first.
 */
@Component
@DependsOn({"groupSeeder"})
@Slf4j
public class DefaultUsersInitializer {
    private final UserTypeFactory userTypeFactory;
    private final UserRepository userRepository;
    
    @Value("${pharmacyhub.superadmin.email:superadmin@pharmacyhub.com}")
    private String superAdminEmail;
    
    @Value("${pharmacyhub.superadmin.password:superadmin123}")
    private String superAdminPassword;
    
    @Value("${pharmacyhub.admin.email:admin@pharmacyhub.com}")
    private String adminEmail;
    
    @Value("${pharmacyhub.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${pharmacyhub.demo.email:demo@pharmacyhub.com}")
    private String demoEmail;
    
    @Value("${pharmacyhub.demo.password:demo123}")
    private String demoPassword;
    
    @Autowired
    public DefaultUsersInitializer(
            UserTypeFactory userTypeFactory,
            UserRepository userRepository) {
        this.userTypeFactory = userTypeFactory;
        this.userRepository = userRepository;
    }
    
    /**
     * Initialize default users if they don't exist.
     */
    @PostConstruct
    @Transactional
    public void initialize() {
        try {
            log.info("Initializing default users...");
            createDefaultSuperAdmin();
            createDefaultAdmin();
            createDefaultDemoUser();
            log.info("Default users initialization completed.");
        } catch (Exception e) {
            log.error("Error initializing default users", e);
        }
    }
    
    /**
     * Creates the default super admin user if it doesn't exist.
     */
    private void createDefaultSuperAdmin() {
        if (userRepository.findByEmailAddress(superAdminEmail).isEmpty()) {
            log.info("Creating default Super Admin user...");
            User superAdmin = userTypeFactory.createSuperAdmin(
                    superAdminEmail,
                    "Super",
                    "Admin",
                    superAdminPassword);
            log.info("Default Super Admin created: {}", superAdmin.getEmailAddress());
        } else {
            log.info("Default Super Admin already exists.");
        }
    }
    
    /**
     * Creates the default admin user if it doesn't exist.
     */
    private void createDefaultAdmin() {
        if (userRepository.findByEmailAddress(adminEmail).isEmpty()) {
            log.info("Creating default Admin user...");
            User admin = userTypeFactory.createAdmin(
                    adminEmail,
                    "Regular",
                    "Admin",
                    adminPassword);
            log.info("Default Admin created: {}", admin.getEmailAddress());
        } else {
            log.info("Default Admin already exists.");
        }
    }
    
    /**
     * Creates the default demo user if it doesn't exist.
     */
    private void createDefaultDemoUser() {
        if (userRepository.findByEmailAddress(demoEmail).isEmpty()) {
            log.info("Creating default Demo user...");
            User demoUser = userTypeFactory.createDemoUser(
                    demoEmail,
                    "Demo",
                    "User",
                    demoPassword);
            log.info("Default Demo User created: {}", demoUser.getEmailAddress());
        } else {
            log.info("Default Demo User already exists.");
        }
    }
}