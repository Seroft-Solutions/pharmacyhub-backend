package com.pharmacyhub.dto.display;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.dto.BaseDTO;
import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.ProprietorDTO;
import com.pharmacyhub.dto.SalesmanDTO;
import com.pharmacyhub.dto.PharmacyManagerDTO;
import com.pharmacyhub.entity.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User response DTO for displaying user information
 * Contains all user properties that should be exposed to clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO implements BaseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String contactNumber;
    private UserType userType;
    private boolean active;
    private boolean openToConnect;
    private boolean registered;
    
    // Associated entities - only included when appropriate
    private PharmacistDTO pharmacist;
    private ProprietorDTO proprietor;
    private SalesmanDTO salesman;
    private PharmacyManagerDTO pharmacyManager;
}
