package com.pharmacyhub.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacyhub.constants.ErrorConstants;
import com.pharmacyhub.dto.response.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom entry point for handling authentication failures (401 Unauthorized)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException, ServletException {
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Log authentication failure details
        log.warn(
            "Authentication failed: {} {} for ip='{}', userAgent='{}', reason='{}'",
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            request.getHeader("User-Agent"),
            ex.getMessage()
        );
        
        // Additional diagnostic information (not exposed to clients)
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("timestamp", LocalDateTime.now().toString());
        diagnostics.put("requestUri", request.getRequestURI());
        diagnostics.put("method", request.getMethod());
        diagnostics.put("exceptionType", ex.getClass().getName());
        diagnostics.put("exceptionMessage", ex.getMessage());
        
        // Log diagnostics separately for internal troubleshooting
        log.debug("401 Authentication Failure Diagnostics: {}", diagnostics);
        
        // Prepare client-facing error response
        String errorCode = ErrorConstants.CODE_AUTHENTICATION;
        String message = ErrorConstants.AUTHENTICATION_FAILED;
        
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
