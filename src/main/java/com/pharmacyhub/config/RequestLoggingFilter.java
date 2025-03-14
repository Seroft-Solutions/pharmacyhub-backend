package com.pharmacyhub.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to log all incoming HTTP requests and outgoing responses
 * Adds request-specific information to the logging context for better traceability
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Generate a unique request ID for tracing
        String requestId = UUID.randomUUID().toString();
        
        // Add context information to MDC for logging
        MDC.put("requestId", requestId);
        MDC.put("method", httpRequest.getMethod());
        MDC.put("path", httpRequest.getRequestURI());
        MDC.put("ip", httpRequest.getRemoteAddr());
        MDC.put("userAgent", httpRequest.getHeader("User-Agent"));
        
        // Add the request ID to the response header for client-side tracing
        httpResponse.setHeader("X-Request-ID", requestId);
        
        try {
            // Log the incoming request
            log.info("Request: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            
            // Process the request
            long startTime = System.currentTimeMillis();
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;
            
            // Log the response
            log.info("Response: {} {} - Status: {} - Duration: {}ms",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration);
            
            // Log detailed information for error responses
            if (httpResponse.getStatus() >= 400) {
                log.warn("Error response: {} {} - Status: {}",
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        httpResponse.getStatus());
            }
        } finally {
            // Clear MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Initializing request logging filter");
    }
    
    @Override
    public void destroy() {
        log.info("Destroying request logging filter");
    }
}
