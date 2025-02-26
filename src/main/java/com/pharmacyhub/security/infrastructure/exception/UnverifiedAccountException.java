package com.pharmacyhub.security.infrastructure.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when a user attempts to login with an unverified account
 */
public class UnverifiedAccountException extends AuthenticationException {
    
    public UnverifiedAccountException(String msg) {
        super(msg);
    }
    
    public UnverifiedAccountException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
