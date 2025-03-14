package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {
    Optional<Feature> findByName(String name);
    boolean existsByName(String name);
}