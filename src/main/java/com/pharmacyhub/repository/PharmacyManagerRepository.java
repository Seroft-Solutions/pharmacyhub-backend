package com.pharmacyhub.repository;

import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyManagerRepository extends JpaRepository<PharmacyManager, Long>
{
  PharmacyManager findByUser(User loggedInUser);
}

