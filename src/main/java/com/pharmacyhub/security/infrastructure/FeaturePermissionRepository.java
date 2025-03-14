package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.Feature;
import com.pharmacyhub.security.domain.FeaturePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeaturePermissionRepository extends JpaRepository<FeaturePermission, Long> {
    List<FeaturePermission> findByFeature(Feature feature);
    
    Optional<FeaturePermission> findByFeatureAndAccessLevel(Feature feature, String accessLevel);
    
    boolean existsByFeatureAndAccessLevel(Feature feature, String accessLevel);
}