package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Set;


@Repository
public interface RolesRepository extends JpaRepository<Role, Long>, RoleRepositoryInterface {
    @Override
    Optional<Role> findByName(String name);

    @Override
    @Query("SELECT r FROM Role r WHERE r.system = true")
    List<Role> findBySystemTrue();

    @Override
    @Query("SELECT r FROM Role r WHERE r.precedence <= :maxPrecedence")
    List<Role> findByPrecedenceLessThanEqual(@Param("maxPrecedence") Integer maxPrecedence);

    @Override
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.childRoles WHERE r.id = :roleId")
    Role findByIdWithChildRoles(Long roleId);
}
