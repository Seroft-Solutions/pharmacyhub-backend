package com.pharmacyhub.security.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType operationType;

    @Column(nullable = false)
    @Builder.Default
    private boolean requiresApproval = false;
    
    // Ensure name is never null
    public String getName() {
        return name != null ? name : "";
    }
    
    // Ensure resourceType is never null
    public ResourceType getResourceType() {
        return resourceType != null ? resourceType : ResourceType.USER;
    }
    
    // Ensure operationType is never null
    public OperationType getOperationType() {
        return operationType != null ? operationType : OperationType.READ;
    }
    
    public String getDescription() {
        return description != null ? description : "";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Permission that = (Permission) o;
        
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        
        if (name == null || that.name == null) {
            return false;
        }
        
        return name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return name != null ? name.hashCode() : 0;
    }
}