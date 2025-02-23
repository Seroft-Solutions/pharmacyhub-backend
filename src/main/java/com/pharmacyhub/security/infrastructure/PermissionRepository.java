package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    
    List<Permission> findByResourceType(ResourceType resourceType);
    
    List<Permission> findByRequiresApprovalTrue();
    
    Set<Permission> findByIdIn(Set<Long> ids);
    
    boolean existsByNameAndIdNot(String name, Long id);
}