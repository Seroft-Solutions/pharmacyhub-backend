package com.pharmacy.hub.repository;

import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesmanRepository extends JpaRepository<Salesman, Long>
{
  Salesman findByUser(User loggedInUser);
}
