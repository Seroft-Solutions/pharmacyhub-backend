package com.pharmacy.hub.controller;

import com.pharmacy.hub.constants.APIConstants;
import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.ProprietorDTO;
import com.pharmacy.hub.dto.ProprietorsConnectionsDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.service.ProprietorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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



  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all-pending-requests", method = RequestMethod.GET)
  public ResponseEntity<List<UserDisplayDTO>> getAllPendingRequests()
  {
    return new ResponseEntity<>(proprietorService.findPendingUsers(), HttpStatus.OK);
  }


  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all-connections", method = RequestMethod.GET)
  public ResponseEntity<List<UserDisplayDTO>> getAllConnections()
  {
    return new ResponseEntity<>(proprietorService.getAllUserConnections(), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/connect",
          method = RequestMethod.POST
  )
  public ResponseEntity connectProprietor(@RequestBody ProprietorsConnectionsDTO proprietorsConnectionsDTO)
  {
    proprietorService.connectWith(proprietorsConnectionsDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }



  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/approveStatus/{id}", method = RequestMethod.POST)
  public ResponseEntity approveStatus(@PathVariable Long id) {
    proprietorService.approveStatus(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(value = APIConstants.API_VERSION_V1 + "/rejectStatus/{id}", method = RequestMethod.POST)
  public ResponseEntity rejectStatus(@PathVariable Long id)
  {

    proprietorService.rejectStatus(id);
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

//  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//  @RequestMapping(
//          value = APIConstants.API_VERSION_V1 + "/get-all-connections",
//          method = RequestMethod.GET
//  )
//  public ResponseEntity getAllConnections()
//  {
//    return new ResponseEntity<>(proprietorService.getAllConnections(), HttpStatus.OK);
//  }

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
