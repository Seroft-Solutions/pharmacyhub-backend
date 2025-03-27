package com.pharmacyhub.service;

import com.pharmacyhub.dto.response.ApiErrorResponse;
import com.pharmacyhub.exception.BaseException;
import com.pharmacyhub.exception.ExceptionConstants;
import com.pharmacyhub.exception.SessionExpiredException;
import com.pharmacyhub.exception.SessionNotFoundException;
import com.pharmacyhub.exception.SessionValidationException;
import com.pharmacyhub.exception.UnauthorizedException;
import com.pharmacyhub.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for handling different types of exceptions and generating standardized error responses.
 * This centralizes exception handling logic outside of the controller advice.
 */
@Service
public class ExceptionHandlerService {
    
    @Autowired
    private LogUtils logUtils;
    
    /**
     * Handle BaseException instances
     */
    public ApiErrorResponse handleBaseException(BaseException ex, HttpServletRequest request, Logger log) {
        logUtils.logException(log, request, ex);
        
        return ApiErrorResponse.builder()
                .status(ex.getStatus().value())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .resolution(ex.getResolution())
                .path(request.getRequestURI())
                .build();
    }
    
    /**
     * Handle authentication exceptions with specific error details based on exception type
     */
    public ApiErrorResponse handleAuthenticationException(AuthenticationException ex, HttpServletRequest request, Logger log) {
        ExceptionConstants exceptionConstant = ExceptionConstants.AUTHENTICATION_FAILED;
        
        // Handle specific authentication exceptions
        if (ex instanceof BadCredentialsException || ex instanceof UsernameNotFoundException) {
            // Keep the default AUTHENTICATION_FAILED
        } else if (ex instanceof DisabledException) {
            exceptionConstant = ExceptionConstants.ACCOUNT_DISABLED;
        } else if (ex instanceof LockedException) {
            exceptionConstant = ExceptionConstants.ACCOUNT_LOCKED;
        } else if (ex instanceof AccountExpiredException) {
            exceptionConstant = ExceptionConstants.ACCOUNT_EXPIRED;
        }
        
        logUtils.logException(log, request, ex);
        return ApiErrorResponse.fromExceptionConstant(exceptionConstant, request.getRequestURI());
    }
    
    /**
     * Handle access denied exceptions
     */
    public ApiErrorResponse handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request, Logger log) {
        logUtils.logAccessDenied(log, request, ex.getMessage());
        
        return ApiErrorResponse.fromExceptionConstant(
                ExceptionConstants.ACCESS_DENIED,
                request.getRequestURI()
        );
    }
    
    /**
     * Handle session-related exceptions
     */
    public ApiErrorResponse handleSessionException(Exception ex, HttpServletRequest request, Logger log) {
        ExceptionConstants exceptionConstant;
        
        if (ex instanceof SessionExpiredException) {
            exceptionConstant = ExceptionConstants.SESSION_EXPIRED;
        } else if (ex instanceof SessionNotFoundException) {
            exceptionConstant = ExceptionConstants.SESSION_NOT_FOUND;
        } else if (ex instanceof SessionValidationException) {
            exceptionConstant = ExceptionConstants.SESSION_VALIDATION_ERROR;
        } else {
            exceptionConstant = ExceptionConstants.SESSION_CONFLICT;
        }
        
        logUtils.logException(log, request, ex);
        return ApiErrorResponse.fromExceptionConstant(exceptionConstant, request.getRequestURI());
    }
    
    /**
     * Handle unauthorized access exceptions
     */
    public ApiErrorResponse handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request, Logger log) {
        logUtils.logException(log, request, ex);
        
        // If the exception has a custom error code, use it; otherwise, use AUTHENTICATION_FAILED
        ExceptionConstants exceptionConstant = ExceptionConstants.AUTHENTICATION_FAILED;
        
        if (ex.getErrorCode().equals(ExceptionConstants.INVALID_TOKEN.getCode())) {
            exceptionConstant = ExceptionConstants.INVALID_TOKEN;
        }
        
        return ApiErrorResponse.builder()
                .status(ex.getStatus().value())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .resolution(ex.getResolution() != null ? ex.getResolution() : exceptionConstant.getResolution())
                .path(request.getRequestURI())
                .build();
    }
    
    /**
     * Handle generic server errors
     */
    public ApiErrorResponse handleInternalServerError(Exception ex, HttpServletRequest request, Logger log) {
        // Log the full exception details for server-side debugging
        logUtils.logException(log, request, ex);
        
        return ApiErrorResponse.fromExceptionConstant(
                ExceptionConstants.INTERNAL_ERROR,
                request.getRequestURI()
        );
    }
}