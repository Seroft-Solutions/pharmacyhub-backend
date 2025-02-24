package com.pharmacyhub.entity;

import com.pharmacyhub.security.domain.Group;
import org.springframework.data.domain.Sort;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pharmacyhub.entity.enums.UserType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String emailAddress;
    
    private String verificationToken;
    private LocalDateTime tokenCreationDate;
    private boolean verified;
    private boolean registered;
    private boolean openToConnect;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private SystemRole systemRole;

    private String firstName;
    private String lastName;
    private String contactNumber;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<com.pharmacyhub.security.domain.Role> roles = new HashSet<>();

    public void setRole(com.pharmacyhub.security.domain.Role role) {
        this.roles.add(role);
    }

    public com.pharmacyhub.security.domain.Role getRole() {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
            .min((r1, r2) -> Integer.compare(r1.getPrecedence(), r2.getPrecedence()))
            .orElse(null);
    }
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_groups",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    private Set<Group> groups = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_permissions_override",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<String> permissionOverrides = new HashSet<>();
    
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;
    
    @Column
    private LocalDateTime passwordExpiryDate;
    
    @Version
    private Long version;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Add role-based authorities
        authorities.addAll(roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toSet()));
        
        // Add permission-based authorities
        roles.forEach(role -> 
            role.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))));
        
        // Add group-based authorities
        groups.forEach(group -> 
            group.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName())));
            }));
            
        return authorities;
    }

    @Override
    public String getUsername() {
        return emailAddress;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (passwordExpiryDate == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(passwordExpiryDate);
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
