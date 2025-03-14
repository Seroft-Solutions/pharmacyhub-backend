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
 * Entity representing a system feature that requires access control
 */
@Entity
@Table(name = "features")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<FeaturePermission> featurePermissions = new HashSet<>();
    
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
    
    // Ensure name is never null
    public String getName() {
        return name != null ? name : "";
    }
    
    public String getDescription() {
        return description != null ? description : "";
    }
    
    public Set<FeaturePermission> getFeaturePermissions() {
        if (featurePermissions == null) {
            return new HashSet<>();
        }
        return featurePermissions;
    }
    
    public void setFeaturePermissions(Set<FeaturePermission> featurePermissions) {
        this.featurePermissions = featurePermissions != null ? featurePermissions : new HashSet<>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Feature feature = (Feature) o;
        
        if (id != null && feature.id != null) {
            return id.equals(feature.id);
        }
        
        return name != null && name.equals(feature.name);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return name != null ? name.hashCode() : 0;
    }
}