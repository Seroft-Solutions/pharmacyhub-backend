package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT r FROM Role r JOIN r.childRoles c WHERE r.id = :roleId")
    Set<Role> findAllChildRoles(@Param("roleId") Long roleId);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    Set<Role> findAllByPermissionName(@Param("permissionName") String permissionName);
}
