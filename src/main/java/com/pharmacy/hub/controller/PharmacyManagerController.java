package com.pharmacy.hub.controller;

import com.pharmacy.hub.constants.APIConstants;
import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.PharmacyManagerConnectionsDTO;
import com.pharmacy.hub.dto.PharmacyManagerDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.entity.connections.PharmacyManagerConnections;
import com.pharmacy.hub.service.PharmacyManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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


  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all-pending-requests", method = RequestMethod.GET)
  public ResponseEntity<List<UserDisplayDTO>> getAllPendingRequests()
  {
    return new ResponseEntity<>(pharmacyManagerService.findPendingUsers(), HttpStatus.OK);
  }


  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all-connections", method = RequestMethod.GET)
  public ResponseEntity<List<UserDisplayDTO>> getAllConnections()
  {
    return new ResponseEntity<>(pharmacyManagerService.getAllUserConnections(), HttpStatus.OK);
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
  public ResponseEntity connectPharmacyManager(@RequestBody PharmacyManagerConnectionsDTO pharmacyManagerConnectionsDTO)
  {
    pharmacyManagerService.connectWith(pharmacyManagerConnectionsDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }


  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/approveStatus/{id}", method = RequestMethod.POST)
  public ResponseEntity approveStatus(@PathVariable Long id) {
    pharmacyManagerService.approveStatus(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/rejectStatus/{id}", method = RequestMethod.POST)
  public ResponseEntity rejectStatus(@PathVariable Long id)
  {

    pharmacyManagerService.rejectStatus(id);
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





