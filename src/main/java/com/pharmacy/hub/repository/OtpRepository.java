package com.pharmacy.hub.repository;

import com.pharmacy.hub.entity.Otp;
import com.pharmacy.hub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long>
{
  Optional<Otp> findTopByUserAndCodeOrderByCreatedAtDesc(User user, String code);
}
