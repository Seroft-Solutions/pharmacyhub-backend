package com.pharmacyhub.repository.connections;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.connections.PharmacistsConnections;
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
