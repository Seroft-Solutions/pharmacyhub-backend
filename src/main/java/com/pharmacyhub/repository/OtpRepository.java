package com.pharmacyhub.repository;

import com.pharmacyhub.entity.Otp;
import com.pharmacyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long>
{
  Optional<Otp> findTopByUserAndCodeOrderByCreatedAtDesc(User user, String code);
}
