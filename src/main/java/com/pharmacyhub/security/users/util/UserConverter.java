package com.pharmacyhub.security.users.util;

import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.entity.User;
import lombok.experimental.UtilityClass;

/**
 * Utility class for converting between User entities and DTOs.
 */
@UtilityClass
public class UserConverter {
    
    /**
     * Convert a User entity to a PHUserDTO.
     * 
     * @param user User entity
     * @return PHUserDTO
     */
    public static PHUserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return PHUserDTO.builder()
                .id(user.getId())
                .emailAddress(user.getEmailAddress())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .contactNumber(user.getContactNumber())
                .userType(user.getUserType())
                .openToConnect(user.isOpenToConnect())
                .registered(user.isRegistered())
                .build();
    }
}
