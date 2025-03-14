package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the Feature entity
 */
@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {
    
    /**
     * Find a feature by its code
     * 
     * @param code The unique code of the feature
     * @return The feature if found
     */
    Optional<Feature> findByCode(String code);
    
    /**
     * Find all active features
     * 
     * @return List of all active features
     */
    List<Feature> findByActiveTrue();
}
