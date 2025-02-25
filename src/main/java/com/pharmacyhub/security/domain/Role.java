package com.pharmacyhub.security.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.pharmacyhub.constants.RoleEnum;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleEnum name;

    @Column
    private String description;
    
    @Column(nullable = false)
    private int precedence;

    @Column(nullable = false)
    @Builder.Default
    private boolean system = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_hierarchy",
        joinColumns = @JoinColumn(name = "parent_role_id"),
        inverseJoinColumns = @JoinColumn(name = "child_role_id")
    )
    @Builder.Default
    private Set<Role> childRoles = new HashSet<>();

    // Override getName to always return the enum's string value, not null
    public String getName() {
        return name != null ? name.toString() : null;
    }
    
    // Add a method to get the RoleEnum directly
    public RoleEnum getRoleEnum() {
        return name;
    }
    
    // Using proper getters/setters instead of relying on Lombok for critical parts
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public int getPrecedence() {
        return precedence;
    }
    
    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }
    
    public boolean isSystem() {
        return system;
    }
    
    public void setSystem(boolean system) {
        this.system = system;
    }
    
    public Set<Permission> getPermissions() {
        if (permissions == null) {
            return new HashSet<>();
        }
        return permissions;
    }
    
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
    
    public Set<Role> getChildRoles() {
        if (childRoles == null) {
            return new HashSet<>();
        }
        return childRoles;
    }
    
    public void setChildRoles(Set<Role> childRoles) {
        this.childRoles = childRoles;
    }
}