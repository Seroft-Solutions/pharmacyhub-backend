package com.pharmacyhub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.entity.enums.UserType;
import lombok.Builder;
import lombok.Data;

/**
 * Common interface for all user DTOs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface PHUserDTO {
    Long getId();
    String getEmailAddress();
    String getFirstName();
    String getLastName();
    String getContactNumber();
    UserType getUserType();
    boolean isOpenToConnect();
    boolean isRegistered();
    
    /**
     * Builder implementation for creating PHUserDTO instances.
     */
    @Data
    class Impl implements PHUserDTO {
        private Long id;
        private String emailAddress;
        private String firstName;
        private String lastName;
        private String contactNumber;
        private UserType userType;
        private boolean openToConnect;
        private boolean registered;
    }
    
    /**
     * Static builder method.
     */
    static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder implementation for PHUserDTO
     */
    class Builder {
        private Long id;
        private String emailAddress;
        private String firstName;
        private String lastName;
        private String contactNumber;
        private UserType userType;
        private boolean openToConnect;
        private boolean registered;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }
        
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public Builder contactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
            return this;
        }
        
        public Builder userType(UserType userType) {
            this.userType = userType;
            return this;
        }
        
        public Builder openToConnect(boolean openToConnect) {
            this.openToConnect = openToConnect;
            return this;
        }
        
        public Builder registered(boolean registered) {
            this.registered = registered;
            return this;
        }
        
        public Impl build() {
            Impl impl = new Impl();
            impl.id = this.id;
            impl.emailAddress = this.emailAddress;
            impl.firstName = this.firstName;
            impl.lastName = this.lastName;
            impl.contactNumber = this.contactNumber;
            impl.userType = this.userType;
            impl.openToConnect = this.openToConnect;
            impl.registered = this.registered;
            return impl;
        }
    }
}
