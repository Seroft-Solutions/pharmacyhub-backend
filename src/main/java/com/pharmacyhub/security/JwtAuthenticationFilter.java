package com.pharmacyhub.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired
  private JwtHelper jwtHelper;


  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // Skip authentication for authentication endpoints
    if (request.getRequestURI().startsWith("/api/auth/") ||
        request.getRequestURI().equals("/api/health") ||
        request.getRequestURI().equals("/health") ||
        request.getRequestURI().startsWith("/api/public/") ||
        request.getRequestURI().startsWith("/swagger-ui/") ||
        request.getRequestURI().startsWith("/v3/api-docs/")) {
      filterChain.doFilter(request, response);
      return;
    }

    String requestHeader = request.getHeader("Authorization");

    // If no Authorization header or not Bearer token, continue with filter chain
    if (requestHeader == null || !requestHeader.startsWith("Bearer ")) {
      logger.debug("No Bearer token found in request");
      filterChain.doFilter(request, response);
      return;
    }
    
    // Extract token
    String token = requestHeader.substring(7);
    String username = null;
    
    try {
      // Get username from token
      username = jwtHelper.getUsernameFromToken(token);
      logger.debug("Token is for user: {}", username);
      
      // If we have valid token and no current authentication
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
          // Load user details
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);
          
          // Validate token
          boolean isTokenValid = jwtHelper.validateToken(token, userDetails);
          
          if (isTokenValid) {
            // Create authentication token
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // Add request details to authentication
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Authentication successful for user: {}", username);
          } else {
            logger.warn("Token validation failed for user: {}", username);
            // Don't return early, let the request continue as unauthenticated
          }
        } catch (UsernameNotFoundException e) {
          logger.error("User not found: {}", username);
          // Don't return early, let the request continue as unauthenticated
        }
      }
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
      // Don't return early, let the request continue as unauthenticated
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
      // Don't return early, let the request continue as unauthenticated
    } catch (Exception e) {
      logger.error("Error processing JWT token: {}", e.getMessage());
      // Don't return early, let the request continue as unauthenticated
    }
    
    filterChain.doFilter(request, response);
  }
  
  /**
   * Helper method to send error response
   */
  private void sendErrorResponse(HttpServletResponse response, int status, String error, String message) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    
    objectMapper.writeValue(response.getOutputStream(), 
        new ErrorResponse(error, message));
  }
  
  /**
   * Simple error response class
   */
  private static class ErrorResponse {
    private final String error;
    private final String message;
    
    public ErrorResponse(String error, String message) {
      this.error = error;
      this.message = message;
    }
    
    public String getError() {
      return error;
    }
    
    public String getMessage() {
      return message;
    }
  }
}