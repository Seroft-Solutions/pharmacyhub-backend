package com.pharmacy.hub.repository.connections;

import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.ProprietorsConnections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Proprietor entity.
 */
@Repository
public interface ProprietorsConnectionsRepository extends JpaRepository<ProprietorsConnections, Long>
{
  List<ProprietorsConnections> findByUserAndProprietorAndState(User user, Proprietor pharmacyManager, StateEnum stateEnum);

  List<ProprietorsConnections> findByUserAndState(User user, StateEnum stateEnum);
}
