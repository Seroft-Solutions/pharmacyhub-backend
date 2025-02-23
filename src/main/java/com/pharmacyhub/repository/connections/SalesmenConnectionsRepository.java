package com.pharmacyhub.repository.connections;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.connections.SalesmenConnections;
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
