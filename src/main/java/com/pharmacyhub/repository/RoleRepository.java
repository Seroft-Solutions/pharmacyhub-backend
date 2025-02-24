package com.pharmacyhub.repository;

import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.constants.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
    
    List<Role> findBySystemTrue();
    
    @Query("SELECT r FROM Role r WHERE r.precedence <= :maxPrecedence")
    List<Role> findByPrecedenceLessThanEqual(Integer maxPrecedence);
    
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id IN :roleIds")
    Set<Role> findByIdInWithPermissions(Set<Long> roleIds);
    
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.childRoles WHERE r.id = :roleId")
    Optional<Role> findByIdWithChildRoles(Long roleId);
    
    boolean existsByNameAndIdNot(RoleEnum name, Long id);
}
