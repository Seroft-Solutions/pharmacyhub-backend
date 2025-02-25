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

    public String getName() {
        return name;
    }
    
    public ResourceType getResourceType() {
        return resourceType != null ? resourceType : ResourceType.USER;
    }
    
    public OperationType getOperationType() {
        return operationType != null ? operationType : OperationType.READ;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Permission that = (Permission) o;
        
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        
        return name != null && name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return name != null ? name.hashCode() : 0;
    }
}