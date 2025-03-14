package com.pharmacyhub.security.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates specific permissions with a feature and creates a named access level
 */
@Entity
@Table(name = "feature_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @Column(nullable = false)
    private String accessLevel;  // e.g., "VIEW", "EDIT", "MANAGE"

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "feature_permission_mappings",
        joinColumns = @JoinColumn(name = "feature_permission_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> requiredPermissions = new HashSet<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Ensure access level is never null
    public String getAccessLevel() {
        return accessLevel != null ? accessLevel : "";
    }
    
    public Set<Permission> getRequiredPermissions() {
        if (requiredPermissions == null) {
            return new HashSet<>();
        }
        return requiredPermissions;
    }
    
    public void setRequiredPermissions(Set<Permission> requiredPermissions) {
        this.requiredPermissions = requiredPermissions != null ? requiredPermissions : new HashSet<>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        FeaturePermission that = (FeaturePermission) o;
        
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        
        if (feature == null || that.feature == null || accessLevel == null || that.accessLevel == null) {
            return false;
        }
        
        return feature.equals(that.feature) && accessLevel.equals(that.accessLevel);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        
        int result = feature != null ? feature.hashCode() : 0;
        result = 31 * result + (accessLevel != null ? accessLevel.hashCode() : 0);
        return result;
    }
}