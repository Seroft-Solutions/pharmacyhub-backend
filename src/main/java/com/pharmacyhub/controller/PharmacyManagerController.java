package com.pharmacyhub.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.PharmacyManagerDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.service.PharmacyManagerService;
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
@RequestMapping(APIConstants.BASE_MAPPING + APIConstants.PHARMACY_MANAGER)
public class PharmacyManagerController
{
  final private int connectCount = 3;

  @Autowired
  private PharmacyManagerService pharmacyManagerService;


  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/add-info",
          method = RequestMethod.POST
  )
  public ResponseEntity<PHUserDTO> addUserInfo(@RequestBody PharmacyManagerDTO pharmacyManagerDTO)
  {
    return new ResponseEntity<PHUserDTO>(pharmacyManagerService.saveUser(pharmacyManagerDTO), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-all",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<UserDisplayDTO>> getAllPharmacyManagers()
  {
    return new ResponseEntity<>(pharmacyManagerService.findAllUsers(), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/connect",
          method = RequestMethod.POST
  )
  public ResponseEntity connectPharmacyManager(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    pharmacyManagerService.connectWith(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-user-connections",
          method = RequestMethod.GET
  )
  public ResponseEntity getPharmacyManagerConnections()
  {
    List<UserDisplayDTO> users = pharmacyManagerService.getAllUserConnections();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/disconnect",
          method = RequestMethod.PUT
  )
  public ResponseEntity disconnectPharmacyManager(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    pharmacyManagerService.disconnectWith(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/user-eligible-to-connect",
          method = RequestMethod.GET
  )
  public ResponseEntity pharmacyManagerConnectCount()
  {
    List<UserDisplayDTO> users = pharmacyManagerService.getAllUserConnections();
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
    return new ResponseEntity<>(pharmacyManagerService.getAllConnections(), HttpStatus.OK);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-state",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateStatus(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    pharmacyManagerService.updateState(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-connection-notes",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateNotes(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    pharmacyManagerService.updateNotes(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}





