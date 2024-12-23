package com.pharmacy.hub.config;

import com.pharmacy.hub.security.TenantContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KeycloakAuditorAware implements AuditorAware<String>
{

    @Override
    public Optional<String> getCurrentAuditor()
    {
        return Optional.ofNullable(TenantContext.getCurrentTenant());
    }
}
