package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, PermissionRepositoryInterface {
    @Override
    Optional<Permission> findByName(String name);
}