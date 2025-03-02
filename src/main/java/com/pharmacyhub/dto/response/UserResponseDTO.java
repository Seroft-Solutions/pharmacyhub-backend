package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * User response DTO that matches frontend User interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO implements BaseDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private String createdAt;
    private String updatedAt;
    
    @Builder.Default
    private List<String> roles = new ArrayList<>();
}
