package com.pharmacy.hub.repository.connections;

import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Pharmacist entity.
 */
@Repository
public interface PharmacistsConnectionsRepository extends JpaRepository<PharmacistsConnections, Long>
{
  List<PharmacistsConnections> findByUserAndPharmacistAndState(User user, Pharmacist pharmacist, StateEnum stateEnum);

  List<PharmacistsConnections> findByUserAndState(User user, StateEnum stateEnum);
}
