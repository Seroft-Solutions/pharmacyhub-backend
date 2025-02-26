package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for access check requests
 * Used to validate if a user has specific roles or permissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessCheckRequest {
    private List<String> roles = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private boolean requireAll = true;
}