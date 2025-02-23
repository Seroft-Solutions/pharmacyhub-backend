package com.pharmacyhub.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.ProprietorDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.service.ProprietorService;
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
@RequestMapping(APIConstants.BASE_MAPPING + APIConstants.PROPRIETOR)
public class ProprietorController
{
  final private int connectCount = 3;

  @Autowired private ProprietorService proprietorService;


  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/add-info",
          method = RequestMethod.POST
  )
  public ResponseEntity<PHUserDTO> addUserInfo(@RequestBody ProprietorDTO proprietorDTO)
  {
    return new ResponseEntity<PHUserDTO>(proprietorService.saveUser(proprietorDTO), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-all",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<UserDisplayDTO>> getAllProprietors()
  {
    return new ResponseEntity<>(proprietorService.findAllUsers(), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/connect",
          method = RequestMethod.POST
  )
  public ResponseEntity connectProprietor(@RequestBody PHUserConnectionDTO PHUserConnectionDTO)
  {
    proprietorService.connectWith(PHUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-user-connections",
          method = RequestMethod.GET
  )
  public ResponseEntity getProprietorUserConnections()
  {
    List<UserDisplayDTO> users = proprietorService.getAllUserConnections();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/disconnect",
          method = RequestMethod.PUT
  )
  public ResponseEntity disconnectProprietor(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    proprietorService.disconnectWith(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/user-eligible-to-connect",
          method = RequestMethod.GET
  )
  public ResponseEntity proprietorConnectCount()
  {
    List<UserDisplayDTO> users = proprietorService.getAllUserConnections();
    if (users.size() < connectCount)
    {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.CONFLICT);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-all-connections",
          method = RequestMethod.GET
  )
  public ResponseEntity getAllConnections()
  {
    return new ResponseEntity<>(proprietorService.getAllConnections(), HttpStatus.OK);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-connection-state",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateStatus(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    proprietorService.updateState(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-connection-notes",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateNotes(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    proprietorService.updateNotes(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
