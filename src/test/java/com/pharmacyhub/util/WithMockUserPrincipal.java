package com.pharmacyhub.util;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TestSecurityUtils.WithMockUserSecurityContextFactory.class)
public @interface WithMockUserPrincipal {
    
    long id() default 1L;
    
    String email() default "test@pharmacyhub.pk";
    
    String firstName() default "Test";
    
    String lastName() default "User";
}
