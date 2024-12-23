package com.pharmacy.hub.repository.connections;

import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.SalesmenConnections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Pharmacist entity.
 */
@Repository
public interface SalesmenConnectionsRepository extends JpaRepository<SalesmenConnections, Long>
{
  List<SalesmenConnections> findByUserAndSalesmanAndState(User user, Salesman salesman, StateEnum stateEnum);

  List<SalesmenConnections> findByUserAndState(User user, StateEnum stateEnum);
}
