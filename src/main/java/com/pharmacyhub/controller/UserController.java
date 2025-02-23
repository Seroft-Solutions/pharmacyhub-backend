package com.pharmacyhub.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.ChangePasswordDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
  public ResponseEntity<PHUserDTO> addUser(@RequestBody PHUserDTO user)
  {
    PHUserDTO userCreated = userService.saveUser(user);

    if (userCreated != null)
    {
      return new ResponseEntity<PHUserDTO>(userCreated, HttpStatus.OK);
    }
    return new ResponseEntity<PHUserDTO>(userCreated, HttpStatus.CONFLICT);
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
  public ResponseEntity changeUserPassword(@RequestBody ChangePasswordDTO changePasswordDTO)
  {
    PHUserDTO user = userService.changeUserPassword(changePasswordDTO);

    if (user != null)
    {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-user-info",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateUserInfo(@RequestBody UserDTO phUserDTO)
  {
    return new ResponseEntity<PHUserDTO>(userService.editUserInformation(phUserDTO), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/user-data",
          method = RequestMethod.GET
  )
  public ResponseEntity<String> isUser()
  {
    if (userService.isUserRole())
    {
      return new ResponseEntity<String>(userService.getUserType(), HttpStatus.OK);
    }
    return new ResponseEntity<String>("admin", HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
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
    return new ResponseEntity<Boolean>(userService.isUserRole(), HttpStatus.OK);
  }

}





