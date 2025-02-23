package com.pharmacy.hub.repository;

import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@SuppressWarnings("unused")
@Repository
public interface ProprietorRepository extends JpaRepository<Proprietor, Long>
{
  Proprietor findByUser(User loggedInUser);
}
