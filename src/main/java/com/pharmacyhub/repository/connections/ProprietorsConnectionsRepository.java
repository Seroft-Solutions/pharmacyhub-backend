package com.pharmacyhub.repository.connections;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.entity.Proprietor;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.connections.ProprietorsConnections;
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
