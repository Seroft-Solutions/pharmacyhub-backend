package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepositoryInterface extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
    
    // Using a better approach to find by name string
    @Query("SELECT r FROM Role r WHERE FUNCTION('UPPER', r.name) = FUNCTION('UPPER', :name)")
    Optional<Role> findByNameIgnoreCase(@Param("name") String name);
    
    // Simple query without CAST which might be causing issues
    @Query("SELECT r FROM Role r")
    List<Role> findAllRoles();

    List<Role> findBySystemTrue();

    List<Role> findByPrecedenceLessThanEqual(Integer maxPrecedence);
    
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.childRoles WHERE r.id = :roleId")
    Role findByIdWithChildRoles(@Param("roleId") Long roleId);
}