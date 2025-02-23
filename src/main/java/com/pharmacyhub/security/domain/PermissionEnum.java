package com.pharmacyhub.security.domain;

public enum PermissionEnum {
    // Pharmacist management
    CREATE_PHARMACIST,
    UPDATE_PHARMACIST,
    VIEW_PHARMACIST,
    VIEW_ALL_PHARMACISTS,
    DELETE_PHARMACIST,

    // Connection management
    MANAGE_CONNECTIONS,
    VIEW_CONNECTIONS,
    VIEW_ALL_CONNECTIONS,
    APPROVE_CONNECTIONS,
    REJECT_CONNECTIONS,

    // Admin operations
    MANAGE_ROLES,
    MANAGE_PERMISSIONS,
    MANAGE_GROUPS,
    VIEW_AUDIT_LOGS,
    MANAGE_SYSTEM_SETTINGS,

    // Audit operations
    VIEW_OWN_AUDIT_LOGS,
    EXPORT_AUDIT_LOGS;

    public String getValue() {
        return this.name();
    }
}
