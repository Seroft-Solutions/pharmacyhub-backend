package com.pharmacyhub.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.ChangePasswordDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.UserProfileResponseDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(APIConstants.BASE_MAPPING)
public class UserController
{
  @Autowired
  private UserService userService;

  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/add-user",
          method = RequestMethod.POST
  )
  public ResponseEntity<PHUserDTO> addUser(@RequestBody UserDTO user)
  {
    PHUserDTO userCreated = userService.saveUser(user);
    if (userCreated != null) {
        return ResponseEntity.ok(userCreated);
    }
    return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-user",
          method = RequestMethod.GET
  )
  public ResponseEntity<PHUserDTO> findUserById()
  {
    PHUserDTO user = userService.getUserCompleteInformation();

    if (user != null)
    {
      return new ResponseEntity<PHUserDTO>(user, HttpStatus.OK);
    }
    return new ResponseEntity<PHUserDTO>(user, HttpStatus.NOT_FOUND);
  }

  /**
   * Endpoint to get the current user's profile information
   * This endpoint is used by the frontend to get the logged-in user's profile
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/users/me")
  public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getCurrentUserProfile() {
    User user = userService.getLoggedInUser();
    
    if (user == null) {
      return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "User not found"));
    }
    
    UserProfileResponseDTO profileDTO = new UserProfileResponseDTO();
    profileDTO.setId(user.getId());
    profileDTO.setUsername(user.getUsername());
    profileDTO.setEmail(user.getEmailAddress());
    profileDTO.setFirstName(user.getFirstName());
    profileDTO.setLastName(user.getLastName());
    profileDTO.setActive(user.isActive());
    profileDTO.setRegistered(user.isRegistered());
    
    // Add roles based on userType and explicit roles
    List<String> roles = new ArrayList<>();
    
    // First check if user has explicitly assigned roles
    if (user.getRoles() != null && !user.getRoles().isEmpty()) {
        // Extract role names from the user's roles
        user.getRoles().forEach(role -> {
            roles.add(role.getName());
        });
    } else {
        // Fallback to mapping from userType if no explicit roles
        UserType userType = user.getUserType();
        
        // Handle null userType gracefully
        if (userType != null) {
            switch (userType) {
                case ADMIN:
                    roles.add("ADMIN");
                    break;
                case SUPER_ADMIN:
                    roles.add("SUPER_ADMIN");
                    roles.add("ADMIN"); // Super admin also has admin privileges
                    break;
                case PHARMACIST:
                    roles.add("PHARMACIST");
                    break;
                // Add other role mappings as needed
                default:
                    roles.add("USER"); // Everyone gets basic user role
                    break;
            }
        } else {
            // Default role if userType is null
            roles.add("USER");
        }
    }
    
    // Set the roles directly in the response
    profileDTO.setRoles(roles);
    
    // Set permissions based on roles and user type
    List<String> permissions = new ArrayList<>();
    
    // Add standard permissions for all authenticated users
    permissions.add("view_profile");
    
    // Admin permissions
    if (roles.contains("ADMIN") || roles.contains("SUPER_ADMIN")) {
        permissions.add("manage_users");
        permissions.add("manage_exams");
        permissions.add("view_reports");
        permissions.add("manage_notifications");
    }
    
    // Student permissions (from social login)
    if (roles.contains("STUDENT")) {
        permissions.add("view_past_papers");
        permissions.add("view_model_papers");
        permissions.add("view_subject_papers");
        permissions.add("view_practice_exams");
    }
    
    // Basic user permissions
    if (roles.contains("USER")) {
        permissions.add("view_dashboard");
    }
    
    // Set permissions in the response
    profileDTO.setPermissions(permissions);
    
    // Include userType in the metadata for backward compatibility
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("userType", user.getUserType() != null ? user.getUserType() : UserType.USER);
    
    return ResponseEntity.ok(
        ApiResponse.success(profileDTO, HttpStatus.OK.value(), metadata)
    );
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-all-users",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<User>> getAllUsers()
  {
    return new ResponseEntity<List<User>>(userService.findAll(), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/change-user-password",
          method = RequestMethod.PUT
  )
  public ResponseEntity<PHUserDTO> changeUserPassword(@RequestBody ChangePasswordDTO changePasswordDTO)
  {
    PHUserDTO updatedUser = userService.changeUserPassword(changePasswordDTO);
    if (updatedUser != null) {
      return ResponseEntity.ok(updatedUser);
    }
    return ResponseEntity.notFound().build();
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-user-info",
          method = RequestMethod.PUT
  )
  public ResponseEntity<PHUserDTO> updateUserInfo(@RequestBody UserDTO phUserDTO)
  {
    PHUserDTO updatedUser = userService.editUserInformation(phUserDTO);
    if (updatedUser != null) {
      return ResponseEntity.ok(updatedUser);
    }
    return ResponseEntity.notFound().build();
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/user-data",
          method = RequestMethod.GET
  )
  public ResponseEntity<UserType> getUserType(Long userId)
  {
    UserType userType = userService.getUserType(userId);
    if (userType != null) {
        return ResponseEntity.ok(userType);
    }
    return ResponseEntity.notFound().build();
  }

  @PreAuthorize("hasPermission(null, 'UPDATE_STATUS')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/toggle-user-status",
          method = RequestMethod.GET
  )
  public ResponseEntity<Boolean> updateUserStatus()
  {
    return new ResponseEntity<Boolean>(userService.updateUserStatus(), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/is-user-registered",
          method = RequestMethod.GET
  )
  public ResponseEntity<Boolean> isUserRegistered()
  {
    User user = userService.getLoggedInUser();
    return ResponseEntity.ok(user != null && user.isRegistered());
  }

}