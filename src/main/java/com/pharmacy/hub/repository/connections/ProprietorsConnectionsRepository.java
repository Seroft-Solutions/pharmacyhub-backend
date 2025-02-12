package com.pharmacy.hub.repository.connections;

import com.pharmacy.hub.constants.ConnectionStatusEnum;
import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.ProprietorsConnections;
import com.pharmacy.hub.entity.connections.SalesmenConnections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Proprietor entity.
 */
@Repository
public interface ProprietorsConnectionsRepository extends JpaRepository<ProprietorsConnections, Long>
{
//  List<ProprietorsConnections> findByUserAndProprietorAndState(User user, Proprietor pharmacyManager, StateEnum stateEnum);

  ProprietorsConnections findByUserId(User requesterId);

  List<ProprietorsConnections> findByProprietorIdAndConnectionStatus(Proprietor currentUserProprietor, ConnectionStatusEnum connectionStatusEnum);
}
