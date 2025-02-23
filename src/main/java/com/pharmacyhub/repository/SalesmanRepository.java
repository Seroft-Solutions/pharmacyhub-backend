package com.pharmacyhub.repository;

import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesmanRepository extends JpaRepository<Salesman, Long>
{
  Salesman findByUser(User loggedInUser);
}
