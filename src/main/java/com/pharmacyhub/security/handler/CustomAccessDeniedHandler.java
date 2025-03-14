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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom handler for 403 Access Denied responses with detailed security diagnostics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                      org.springframework.security.access.AccessDeniedException ex) throws IOException, ServletException {
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Get current authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        
        // Log detailed security diagnostics
        log.error(
            "Access denied: {} {} denied for user='{}', ip='{}', userAgent='{}', authorities={}, reason='{}'",
            request.getMethod(),
            request.getRequestURI(),
            username,
            request.getRemoteAddr(),
            request.getHeader("User-Agent"),
            authentication != null ? authentication.getAuthorities() : "none",
            ex.getMessage()
        );
        
        // Additional diagnostic information (not exposed to clients)
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("timestamp", LocalDateTime.now().toString());
        diagnostics.put("user", username);
        diagnostics.put("authorities", authentication != null ? authentication.getAuthorities().toString() : "none");
        diagnostics.put("requestUri", request.getRequestURI());
        diagnostics.put("method", request.getMethod());
        diagnostics.put("exceptionType", ex.getClass().getName());
        diagnostics.put("exceptionMessage", ex.getMessage());
        
        // Log diagnostics separately for internal troubleshooting
        log.debug("403 Access Denied Diagnostics: {}", diagnostics);
        
        // Prepare client-facing error response
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode(ErrorConstants.CODE_ACCESS_DENIED)
                .message(ErrorConstants.ACCESS_DENIED)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
