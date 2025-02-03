package com.pharmacy.hub.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class TenantContext
{
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    private static final InheritableThreadLocal<String> currentTenant = new InheritableThreadLocal<>();


    public static void clear()
    {
        currentTenant.remove();
        logger.debug("Tenant context cleared");
    }

    public static String getCurrentTenant()
    {
        String tenant = currentTenant.get();
        if (tenant == null)
        {
            tenant = extractTenantFromSecurityContext();
            if (tenant != null)
            {
                setCurrentTenant(tenant);
            }
        }
        return tenant;
    }

    public static void setCurrentTenant(String tenantId)
    {
        if (tenantId != null)
        {
            currentTenant.set(tenantId);
            logger.debug("Tenant context set to: {}", tenantId);
        }
    }

    private static String extractTenantFromSecurityContext()
    {
        String userName = "";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken oauthToken)
        {
            userName = oauthToken.getPrincipal().getAttribute("sub");
        }
        else if (auth instanceof JwtAuthenticationToken jwtToken)
        {
            userName = ((Jwt) (jwtToken.getPrincipal())).getClaims().get("sub").toString();
        }


        return userName;
    }
}