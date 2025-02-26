package com.pharmacyhub.controller;

import com.pharmacyhub.dto.LoggedInUserDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.model.LoginRequest;
import com.pharmacyhub.security.service.AuthenticationService;
import com.pharmacyhub.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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
    public ResponseEntity<String> verifyEmail(@RequestParam String token)
    {
        boolean isVerified = userService.verifyUser(token);
        if (isVerified)
        {
            return ResponseEntity.status(HttpStatus.FOUND)
                                 .header(HttpHeaders.LOCATION, "https://pharmacyhub.pk/verification-successful")
                                 .build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.FOUND)
                                 .header(HttpHeaders.LOCATION, "https://pharmacyhub.pk/verification-failed")
                                 .build();
        }
    }


    @RequestMapping(
            value = "/test",
            method = RequestMethod.GET
    )
    public ResponseEntity<List<User>> test()
    {
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request)
    {
        // Authenticate the user - this will throw exceptions if authentication fails
        // or if account is not verified, which are handled by the global exception handler
        User authenticatedUser = authenticationService.authenticateUser(request.getUsername(), request.getPassword());

        // Generate JWT token
        String token = authenticationService.generateToken(authenticatedUser);

        // Get user roles
        Set<Role> userRoles = authenticatedUser.getRoles();
        List<String> roleNames = userRoles.stream()
                                          .map(r -> r.getName())
                                          .collect(Collectors.toList());

        // Get user permissions
        Set<String> permissionNames = new HashSet<>();
        for (Role userRole : userRoles)
        {
            if (userRole.getPermissions() != null)
            {
                userRole.getPermissions().stream()
                        .map(Permission::getName)
                        .forEach(permissionNames::add);
            }
        }

        // Create response DTO
        LoggedInUserDTO response = LoggedInUserDTO.builder()
                                                  .id(authenticatedUser.getId())
                                                  .emailAddress(authenticatedUser.getEmailAddress())
                                                  .firstName(authenticatedUser.getFirstName())
                                                  .lastName(authenticatedUser.getLastName())
                                                  .openToConnect(authenticatedUser.isOpenToConnect())
                                                  .registered(authenticatedUser.isRegistered())
                                                  .userType(authenticatedUser.getUserType())
                                                  .jwtToken(token)
                                                  .roles(roleNames)
                                                  .permissions(new ArrayList<>(permissionNames))
                                                  .build();

        logger.info("Login successful for user: {}", authenticatedUser.getUsername());
        return ResponseEntity.ok(response);
    }


}
