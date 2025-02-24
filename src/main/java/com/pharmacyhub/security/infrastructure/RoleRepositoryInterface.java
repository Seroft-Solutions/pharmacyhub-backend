package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepositoryInterface extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    List<Role> findBySystemTrue();

    List<Role> findByPrecedenceLessThanEqual(Integer maxPrecedence);
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.childRoles WHERE r.id = :roleId")
    Role findByIdWithChildRoles(@Param("roleId") Long roleId);
}
