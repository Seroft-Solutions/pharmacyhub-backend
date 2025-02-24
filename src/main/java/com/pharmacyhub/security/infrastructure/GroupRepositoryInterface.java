package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Group;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepositoryInterface extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
}