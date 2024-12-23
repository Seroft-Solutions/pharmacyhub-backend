package com.pharmacy.hub.controller;

import com.pharmacy.hub.constants.APIConstants;
import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.SalesmanDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.service.SalesmanService;
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
@RequestMapping(APIConstants.BASE_MAPPING + APIConstants.SALESMAN)
public class SalesmanController
{
  final private int connectCount = 3;

  @Autowired private SalesmanService salesmanService;

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/add-info",
          method = RequestMethod.POST
  )
  public ResponseEntity<PHUserDTO> addUserInfo(@RequestBody SalesmanDTO salesmanDTO)
  {
    return new ResponseEntity<PHUserDTO>(salesmanService.saveUser(salesmanDTO), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-all",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<UserDisplayDTO>> getAllSalesman()
  {
    return new ResponseEntity<List<UserDisplayDTO>>(salesmanService.findAllUsers(), HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/connect",
          method = RequestMethod.POST
  )
  public ResponseEntity connectSalesman(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    salesmanService.connectWith(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-user-connections",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<UserDisplayDTO>> getUserSalesmanConnections()
  {
    List<UserDisplayDTO> users = salesmanService.getAllUserConnections();
    return new ResponseEntity<List<UserDisplayDTO>>(users, HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/disconnect",
          method = RequestMethod.PUT
  )
  public ResponseEntity disconnectSalesman(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    salesmanService.disconnectWith(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/user-eligible-to-connect",
          method = RequestMethod.GET
  )
  public ResponseEntity isUserEligibleToConnectSalesman()
  {
    List<UserDisplayDTO> users = salesmanService.getAllUserConnections();
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
    return new ResponseEntity<>(salesmanService.getAllConnections(), HttpStatus.OK);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-connection-state",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateStatus(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    salesmanService.updateState(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-connection-notes",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateNotes(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    salesmanService.updateNotes(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}





