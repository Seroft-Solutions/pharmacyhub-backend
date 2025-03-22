package com.pharmacyhub.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for OpenAPI related endpoints
 */
@RestController
@RequestMapping("/api/openapi")
public class OpenApiController {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiController.class);
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Generates and downloads the OpenAPI spec JSON file
     * 
     * @param request The HTTP request
     * @return The OpenAPI spec JSON file
     */
    @GetMapping("/download-spec")
    public ResponseEntity<String> downloadOpenApiSpec(HttpServletRequest request) {
        try {
            // Determine the base URL
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            
            // Fetch the OpenAPI spec from the API docs endpoint
            String apiDocsUrl = baseUrl + "/api-docs";
            logger.info("Fetching OpenAPI spec from: {}", apiDocsUrl);
            
            String openApiSpec = restTemplate.getForObject(apiDocsUrl, String.class);
            
            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "openapi-spec.json");
            
            // Also save to src/main/resources/static/openapi-spec.json
            try {
                Path resourcesPath = Paths.get("src/main/resources/static");
                if (!Files.exists(resourcesPath)) {
                    Files.createDirectories(resourcesPath);
                }
                
                Path filePath = resourcesPath.resolve("openapi-spec.json");
                Files.writeString(filePath, openApiSpec, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                logger.info("OpenAPI spec saved to: {}", filePath.toAbsolutePath());
            } catch (IOException e) {
                logger.warn("Could not save OpenAPI spec to resources directory: {}", e.getMessage());
            }
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(openApiSpec);
        } catch (Exception e) {
            logger.error("Error generating OpenAPI spec: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error generating OpenAPI spec: " + e.getMessage());
        }
    }
}
