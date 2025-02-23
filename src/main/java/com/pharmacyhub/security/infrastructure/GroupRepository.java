package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT DISTINCT g FROM Group g JOIN g.roles r JOIN r.permissions p WHERE p.name = :permissionName")
    Set<Group> findAllByPermissionName(@Param("permissionName") String permissionName);
}
