package com.pharmacyhub.controller;

import com.pharmacyhub.dto.LoggedInUserDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.JwtHelper;
import com.pharmacyhub.security.model.LoginRequest;
import com.pharmacyhub.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
  @Autowired
  private UserDetailsService userDetailsService;
  @Autowired
  private AuthenticationManager manager;
  @Autowired
  private JwtHelper helper;
  @Autowired
  private UserService userService;

  private Logger logger = LoggerFactory.getLogger(AuthController.class);

  @RequestMapping(
          value = "/signup",
          method = RequestMethod.POST
  )
  public ResponseEntity<?> addUser(@RequestBody UserDTO user)
  {
    PHUserDTO userCreated = userService.saveUser(user);

    if (userCreated != null)
    {
      return ResponseEntity.ok("User registered successfully. Please check your email for verification.");
    }
    return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this email already exists");
  }

  @GetMapping("/verify")
  public ResponseEntity<String> verifyEmail(@RequestParam String token) {
    boolean isVerified = userService.verifyUser(token);
    if (isVerified) {
      return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "https://pharmacyhub.pk/verification-successful").build();
    } else {
      return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "https://pharmacyhub.pk/verification-failed").build();
    }
  }


  @RequestMapping(
          value = "/test",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<User>> test()
  {
    return new ResponseEntity<>( userService.getUsers(),HttpStatus.OK);
  }

  @RequestMapping(
          value = "/login",
          method = RequestMethod.POST
  )
  public ResponseEntity<LoggedInUserDTO> login(@RequestBody LoginRequest request)
  {
    this.doAuthenticate(request.getEmailAddress(), request.getPassword());

    UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmailAddress());
    String token = this.helper.generateToken(userDetails);

    User loggedInUser = (User) userDetails;
    Role role = loggedInUser.getRole();

    LoggedInUserDTO loggedInUserDTO = LoggedInUserDTO.builder()
            .openToConnect(loggedInUser.isOpenToConnect())
            .isRegistered(loggedInUser.isRegistered())
            .userType(loggedInUser.getUserType())
            .jwtToken(token)
            .build();


    logger.info("JWT token is created for this email", userDetails.getUsername());
    return new ResponseEntity<>(loggedInUserDTO, HttpStatus.OK);
  }

  private void doAuthenticate(String email, String password)
  {
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
    try
    {
      manager.authenticate(authentication);
    }
    catch (BadCredentialsException e)
    {
      throw new BadCredentialsException("Invalid Username or Password  !!");
    }
  }

  @ExceptionHandler(BadCredentialsException.class)
  public String exceptionHandler()
  {
    return "Credentials Invalid !!";
  }

}
