package com.pharmacyhub.security.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Feature entity
 * Represents a functional feature of the application
 * Features can have permissions, operations, and be organized in a hierarchy
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
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "feature_permissions",
        joinColumns = @JoinColumn(name = "feature_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_feature_id")
    private Feature parentFeature;
    
    @OneToMany(mappedBy = "parentFeature", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Feature> childFeatures = new HashSet<>();
    
    /**
     * Operations supported by this feature
     * Common operations include: READ, WRITE, DELETE, MANAGE, etc.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "feature_operations", joinColumns = @JoinColumn(name = "feature_id"))
    @Column(name = "operation")
    @Builder.Default
    private Set<String> operations = new HashSet<>();
}
