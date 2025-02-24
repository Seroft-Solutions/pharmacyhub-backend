package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepositoryInterface extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}