package com.pharmacyhub.repository;

import com.pharmacyhub.entity.Proprietor;
import com.pharmacyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@SuppressWarnings("unused")
@Repository
public interface ProprietorRepository extends JpaRepository<Proprietor, Long>
{
  Proprietor findByUser(User loggedInUser);
}
