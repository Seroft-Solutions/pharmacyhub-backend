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
    @Builder
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
    static PHUserDTOBuilder builder() {
        return new Impl.ImplBuilder();
    }
}
