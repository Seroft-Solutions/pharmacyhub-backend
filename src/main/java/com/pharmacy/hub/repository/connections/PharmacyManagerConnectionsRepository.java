package com.pharmacy.hub.repository.connections;

import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.entity.PharmacyManager;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacyManagerConnections;
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
