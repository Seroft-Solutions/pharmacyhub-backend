package com.pharmacyhub.security;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.service.RBACService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RBACService rbacService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByEmailAddress(username)
                                  .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Get effective permissions
        Set<Permission> effectivePermissions = rbacService.getUserEffectivePermissions(user.getId());

        // Add permission-based authorities
        Set<SimpleGrantedAuthority> authorities = effectivePermissions.stream()
                                                                      .map(permission -> new SimpleGrantedAuthority(
                                                                              permission.getName()))
                                                                      .collect(Collectors.toSet());

        // Add role-based authorities
        user.getRoles().forEach(role ->
                                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));

        // Add group-based authorities
        user.getGroups().forEach(group ->
                                         group.getRoles().forEach(role ->
                                                                          authorities.add(new SimpleGrantedAuthority(
                                                                                  "ROLE_" + role.getName()))));

        return user;
    }
}