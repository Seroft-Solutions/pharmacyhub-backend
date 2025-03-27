package com.pharmacyhub.constants;

public enum RoleEnum {
    USER("USER"),
    ADMIN("ADMIN"),
    SUPER_ADMIN("SUPER_ADMIN"),
    PHARMACIST("PHARMACIST"),
    PHARMACY_MANAGER("PHARMACY_MANAGER"),
    PROPRIETOR("PROPRIETOR"),
    SALESMAN("SALESMAN"),
    INVENTORY_MANAGER("INVENTORY_MANAGER"),
    INSTRUCTOR("INSTRUCTOR"),
    STUDENT("STUDENT"),
    ROLE_USER("ROLE_USER"),
    EXAM_CREATOR("EXAM_CREATOR");
    
    private final String value;
    
    RoleEnum(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public static RoleEnum fromString(String text) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.value.equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant " + text);
    }
}