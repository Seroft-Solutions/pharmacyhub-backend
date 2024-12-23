package com.pharmacy.hub.security;


import com.pharmacy.hub.keycloak.services.KeycloakGroupService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserGroupCheckFilter extends OncePerRequestFilter
{
    private static final Logger logger = LoggerFactory.getLogger(UserGroupCheckFilter.class);

    private final KeycloakGroupService keycloakGroupService;
    private final ThreadPoolTaskExecutor executor;

    public UserGroupCheckFilter(KeycloakGroupService keycloakGroupService,
                                @Qualifier("getAsyncExecutor") ThreadPoolTaskExecutor executor)
    {
        this.keycloakGroupService = keycloakGroupService;
        this.executor = executor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        try
        {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth instanceof OAuth2AuthenticationToken oauthToken)
            {
                String userId = oauthToken.getPrincipal().getAttribute("sub");
                if (userId != null)
                {
                    String username = oauthToken.getPrincipal().getAttribute("preferred_username");
                    TenantContext.setCurrentTenant(username);
                    keycloakGroupService.checkAndCreateUserChildGroup(userId);

                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error in UserGroupCheckFilter: ", e);
        }

        filterChain.doFilter(request, response);
    }
}