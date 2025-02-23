package com.pharmacyhub.repository;

import com.pharmacyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
  Optional<User> findByEmailAddress(String emailAddress);
  Optional<User> findByVerificationToken(String token);
  Optional<User> findById(Long id);
}
