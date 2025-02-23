package com.pharmacyhub.controller;

import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.service.OtpService;
import com.pharmacyhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otp")
public class OtpController
{
  @Autowired
  private OtpService otpService;
  @Autowired
  private UserService userService;

  @PostMapping("/send")
  public ResponseEntity<?> sendOtp(@RequestBody UserDTO userDTO)
  {
    otpService.generateOtp(userDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/validate")
  public ResponseEntity<?> validateOtp(@RequestBody UserDTO userDTO)
  {
    boolean isValid = otpService.validateOtp(userDTO);
    if (isValid)
    {
      return ResponseEntity.ok().body("OTP is valid");
    }
    else
    {
      return ResponseEntity.badRequest().body("Invalid OTP");
    }
  }

  @PostMapping("/reset-user-password")
  public ResponseEntity resetUserPassword(@RequestBody UserDTO userDTO)
  {
    boolean passwordChanged = userService.forgotPassword(userDTO);

    if (passwordChanged)
    {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }
}
