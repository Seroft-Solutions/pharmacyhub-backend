package com.pharmacyhub.controller;

import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.exception.BadRequestException;
import com.pharmacyhub.exception.ForbiddenException;
import com.pharmacyhub.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo controller for testing exception handling
 * Contains endpoints that trigger different types of exceptions
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "Error Handler Test", description = "Endpoints for testing error handling")
@Validated
@Slf4j
public class ErrorDemoController {
    
    @GetMapping("/success")
    @Operation(summary = "Test successful response")
    public ResponseEntity<ApiResponse<String>> testSuccess() {
        log.info("Processing successful request");
        return ResponseEntity.ok(ApiResponse.of("Request processed successfully"));
    }
    
    @GetMapping("/bad-request")
    @Operation(summary = "Test 400 Bad Request")
    public ResponseEntity<ApiResponse<String>> testBadRequest() {
        log.info("Testing bad request exception");
        throw new BadRequestException("This is a demonstration of a Bad Request exception");
    }
    
    @GetMapping("/not-found/{id}")
    @Operation(summary = "Test 404 Not Found")
    public ResponseEntity<ApiResponse<String>> testNotFound(@PathVariable @Min(1) Long id) {
        log.info("Testing resource not found exception for id: {}", id);
        throw new ResourceNotFoundException("Resource", "id", id);
    }
    
    @GetMapping("/forbidden")
    @Operation(summary = "Test 403 Forbidden")
    public ResponseEntity<ApiResponse<String>> testForbidden() {
        log.info("Testing forbidden exception");
        throw new ForbiddenException("This is a demonstration of a Forbidden exception");
    }
    
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Test endpoint with Spring Security @PreAuthorize")
    public ResponseEntity<ApiResponse<String>> adminOnlyEndpoint() {
        log.info("Accessing admin-only endpoint");
        return ResponseEntity.ok(ApiResponse.of("You have ADMIN access"));
    }
    
    @GetMapping("/error")
    @Operation(summary = "Test 500 Internal Server Error")
    public ResponseEntity<ApiResponse<String>> testServerError() {
        log.info("Testing server error");
        throw new RuntimeException("This is a demonstration of an internal server error");
    }
}
