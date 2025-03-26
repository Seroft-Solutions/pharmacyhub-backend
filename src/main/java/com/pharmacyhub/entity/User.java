package com.pharmacyhub.entity;

import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.domain.Permission;
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
    private String profilePictureUrl;
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
    private Set<Role> roles = new HashSet<>();

    public void setRole(Role role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new HashSet<>();
            }
            this.roles.add(role);
        }
    }

    public Role getRole() {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
            .min((r1, r2) -> Integer.compare(
                r1 != null ? r1.getPrecedence() : Integer.MAX_VALUE, 
                r2 != null ? r2.getPrecedence() : Integer.MAX_VALUE))
            .orElse(null);
    }
    
    public Set<Role> getRoles() {
        if (roles == null) {
            return new HashSet<>();
        }
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_groups",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    private Set<Group> groups = new HashSet<>();
    
    public Set<Group> getGroups() {
        if (groups == null) {
            return new HashSet<>();
        }
        return groups;
    }
    
    public void setGroups(Set<Group> groups) {
        this.groups = groups != null ? groups : new HashSet<>();
    }
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_permissions_override",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<String> permissionOverrides = new HashSet<>();
    
    public Set<String> getPermissionOverrides() {
        if (permissionOverrides == null) {
            return new HashSet<>();
        }
        return permissionOverrides;
    }
    
    public void setPermissionOverrides(Set<String> permissionOverrides) {
        this.permissionOverrides = permissionOverrides != null ? permissionOverrides : new HashSet<>();
    }
    
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
        
        // Add role-based authorities - handling null safety
        if (roles != null) {
            for (Role role : roles) {
                if (role != null) {
                    if (role.getName() != null && !role.getName().isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                    } else if (role.getRoleEnum() != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().toString()));
                    }
                    
                    // Add permission-based authorities
                    if (role.getPermissions() != null) {
                        for (Permission permission : role.getPermissions()) {
                            if (permission != null && permission.getName() != null) {
                                authorities.add(new SimpleGrantedAuthority(permission.getName()));
                            }
                        }
                    }
                }
            }
        }
        
        // Add group-based authorities
        if (groups != null) {
            for (Group group : groups) {
                if (group != null && group.getRoles() != null) {
                    for (Role role : group.getRoles()) {
                        if (role != null) {
                            if (role.getName() != null && !role.getName().isEmpty()) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                            } else if (role.getRoleEnum() != null) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().toString()));
                            }
                            
                            if (role.getPermissions() != null) {
                                for (Permission permission : role.getPermissions()) {
                                    if (permission != null && permission.getName() != null) {
                                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Ensure we have at least one role based on user type
        if (authorities.isEmpty() && userType != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userType.name()));
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return emailAddress;
    }
    
    public void setUsername(String username) {
        this.emailAddress = username;
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
    
    public void setEnabled(boolean enabled) {
        this.active = enabled;
    }

    @Override
    public String getPassword() { 
        return password; 
    }
    
    // Explicitly adding these methods to ensure they're available
    public boolean isOpenToConnect() {
        return openToConnect;
    }
    
    public boolean isRegistered() {
        return registered;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }
    
    public String getFirstName() {
        return firstName != null ? firstName : "";
    }
    
    public String getLastName() {
        return lastName != null ? lastName : "";
    }
    
    public String getContactNumber() {
        return contactNumber != null ? contactNumber : "";
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}