package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for feature access check requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureAccessRequest {
    private List<String> features = new ArrayList<>();
    private boolean requireAll = false;
}