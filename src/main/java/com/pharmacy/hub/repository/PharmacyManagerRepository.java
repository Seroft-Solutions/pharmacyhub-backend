package com.pharmacy.hub.repository;

import com.pharmacy.hub.entity.PharmacyManager;
import com.pharmacy.hub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyManagerRepository extends JpaRepository<PharmacyManager, Long>
{
  PharmacyManager findByUser(User loggedInUser);
}

