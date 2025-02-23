package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    
    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.roles WHERE g.id IN :groupIds")
    Set<Group> findByIdInWithRoles(Set<Long> groupIds);
    
    boolean existsByNameAndIdNot(String name, Long id);
}