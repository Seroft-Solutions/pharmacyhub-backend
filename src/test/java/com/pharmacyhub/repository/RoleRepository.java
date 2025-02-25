package com.pharmacyhub.repository;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Test repository for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
    
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByNameAsString(@Param("name") RoleEnum name);

    List<Role> findBySystemTrue();

    List<Role> findByPrecedenceLessThanEqual(Integer maxPrecedence);
}