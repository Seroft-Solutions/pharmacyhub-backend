package com.pharmacyhub.security.users.util;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.users.factory.UserTypeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A utility class for easily creating users of different types.
 * Provides a fluent interface for user creation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreator {
    private final UserTypeFactory userTypeFactory;
    
    /**
     * Create a super admin user.
     */
    public SuperAdminBuilder superAdmin() {
        return new SuperAdminBuilder(userTypeFactory);
    }
    
    /**
     * Create an admin user.
     */
    public AdminBuilder admin() {
        return new AdminBuilder(userTypeFactory);
    }
    
    /**
     * Create a demo user.
     */
    public DemoUserBuilder demoUser() {
        return new DemoUserBuilder(userTypeFactory);
    }
    
    /**
     * Base builder class for all user types.
     */
    public abstract static class UserBuilder<T extends UserBuilder<T>> {
        protected final UserTypeFactory userTypeFactory;
        protected String email;
        protected String firstName;
        protected String lastName;
        protected String password;
        protected String contactNumber;
        
        protected UserBuilder(UserTypeFactory userTypeFactory) {
            this.userTypeFactory = userTypeFactory;
        }
        
        /**
         * Set the email address.
         */
        @SuppressWarnings("unchecked")
        public T withEmail(String email) {
            this.email = email;
            return (T) this;
        }
        
        /**
         * Set the first name.
         */
        @SuppressWarnings("unchecked")
        public T withFirstName(String firstName) {
            this.firstName = firstName;
            return (T) this;
        }
        
        /**
         * Set the last name.
         */
        @SuppressWarnings("unchecked")
        public T withLastName(String lastName) {
            this.lastName = lastName;
            return (T) this;
        }
        
        /**
         * Set the password.
         */
        @SuppressWarnings("unchecked")
        public T withPassword(String password) {
            this.password = password;
            return (T) this;
        }
        
        /**
         * Set the contact number.
         */
        @SuppressWarnings("unchecked")
        public T withContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
            return (T) this;
        }
        
        /**
         * Build the user.
         */
        public abstract User build();
    }
    
    /**
     * Builder for super admin users.
     */
    public static class SuperAdminBuilder extends UserBuilder<SuperAdminBuilder> {
        public SuperAdminBuilder(UserTypeFactory userTypeFactory) {
            super(userTypeFactory);
        }
        
        @Override
        public User build() {
            return userTypeFactory.createSuperAdmin(email, firstName, lastName, password, contactNumber);
        }
    }
    
    /**
     * Builder for admin users.
     */
    public static class AdminBuilder extends UserBuilder<AdminBuilder> {
        public AdminBuilder(UserTypeFactory userTypeFactory) {
            super(userTypeFactory);
        }
        
        @Override
        public User build() {
            return userTypeFactory.createAdmin(email, firstName, lastName, password, contactNumber);
        }
    }
    
    /**
     * Builder for demo users.
     */
    public static class DemoUserBuilder extends UserBuilder<DemoUserBuilder> {
        public DemoUserBuilder(UserTypeFactory userTypeFactory) {
            super(userTypeFactory);
        }
        
        @Override
        public User build() {
            return userTypeFactory.createDemoUser(email, firstName, lastName, password, contactNumber);
        }
    }
}