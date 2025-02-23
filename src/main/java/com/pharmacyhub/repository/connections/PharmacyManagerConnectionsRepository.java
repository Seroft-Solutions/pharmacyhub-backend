package com.pharmacyhub.repository.connections;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.connections.PharmacyManagerConnections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Pharmacist entity.
 */
@Repository
public interface PharmacyManagerConnectionsRepository extends JpaRepository<PharmacyManagerConnections, Long>
{
  List<PharmacyManagerConnections> findByUserAndPharmacyManagerAndState(User user, PharmacyManager pharmacyManager, StateEnum stateEnum);

  List<PharmacyManagerConnections> findByUserAndState(User user, StateEnum stateEnum);
}
