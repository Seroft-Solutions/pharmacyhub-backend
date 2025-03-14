package com.pharmacyhub.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * Utility class for logging with standardized formats
 */
public class LogUtils {
    
    /**
     * Private constructor to prevent instantiation
     */
    private LogUtils() {
        // Utility class, should not be instantiated
    }
    
    /**
     * Log an exception with detailed request information
     */
    public static void logException(Logger logger, HttpServletRequest request, Exception ex) {
        try {
            // Add context information to MDC for logging
            MDC.put("ip", request.getRemoteAddr());
            MDC.put("user", getUserInfo(request));
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());
            MDC.put("userAgent", request.getHeader("User-Agent"));
            
            // Log the exception
            logger.error("Exception: {}, Message: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
            
            // Clear MDC to prevent memory leaks
            MDC.clear();
        } catch (Exception e) {
            // Fallback logging in case something goes wrong with our logging
            logger.error("Exception during request processing: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Log an access denied event with detailed request information
     */
    public static void logAccessDenied(Logger logger, HttpServletRequest request, String reason) {
        try {
            // Add context information to MDC for logging
            MDC.put("ip", request.getRemoteAddr());
            MDC.put("user", getUserInfo(request));
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());
            MDC.put("userAgent", request.getHeader("User-Agent"));
            
            // Log the access denied event
            logger.warn("Access denied: {}", reason);
            
            // Clear MDC to prevent memory leaks
            MDC.clear();
        } catch (Exception e) {
            // Fallback logging in case something goes wrong with our logging
            logger.warn("Access denied during request processing: {}", reason);
        }
    }
    
    /**
     * Extract user information from the request
     */
    private static String getUserInfo(HttpServletRequest request) {
        return Optional.ofNullable(request.getUserPrincipal())
                .map(principal -> principal.getName())
                .orElse("anonymous");
    }
}
