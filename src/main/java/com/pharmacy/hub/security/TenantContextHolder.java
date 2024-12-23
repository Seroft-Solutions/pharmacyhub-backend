package com.pharmacy.hub.security;

import org.springframework.core.task.TaskDecorator;

public class TenantContextHolder implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        String tenantId = TenantContext.getCurrentTenant();
        return () -> {
            try {
                TenantContext.setCurrentTenant(tenantId);
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}