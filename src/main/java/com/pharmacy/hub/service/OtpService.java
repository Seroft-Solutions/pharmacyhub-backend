package com.pharmacy.hub.service;

import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.UserDTO;
import com.pharmacy.hub.entity.Otp;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import jakarta.mail.MessagingException;

@Service
public class OtpService
{
  @Autowired
  private OtpRepository otpRepository;
  @Autowired
  private UserService userService;
  @Autowired
  private EmailService emailService;

  public Otp generateOtp(PHUserDTO userDTO)
  {
    User user = userService.getUserByEmailAddress((UserDTO) userDTO);
    if (user != null)
    {
      Otp otp = new Otp();
      otp.setUser(user);
      otp.setCode(generateRandomOtpCode());
      otp.setCreatedAt(LocalDateTime.now());
      otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
      otpRepository.save(otp);

      sendOtpToUser(otp);
      return otp;
    }
    return null;
  }

  private void sendOtpToUser(Otp otp)
  {
    try
    {
      emailService.sendHtmlMail(otp);
    }
    catch (MessagingException e)
    {
      throw new RuntimeException(e);
    }
  }

  public boolean validateOtp(UserDTO userDTO)
  {
    User user = userService.getUserByEmailAddress(userDTO);
    if (user != null)
    {
      Optional<Otp> otpOptional = otpRepository.findTopByUserAndCodeOrderByCreatedAtDesc(user, userDTO.getOtpCode());
      if (otpOptional.isPresent())
      {
        Otp otp = otpOptional.get();
        return !otp.getExpiresAt().isBefore(LocalDateTime.now());
      }
    }
    return false;
  }

  private String generateRandomOtpCode()
  {
    Random random = new Random();
    int otp = 100000 + random.nextInt(900000);
    return String.valueOf(otp);
  }
}
