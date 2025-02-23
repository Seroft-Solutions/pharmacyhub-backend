package com.pharmacyhub.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.service.PharmacistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.ResourceType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(APIConstants.BASE_MAPPING + APIConstants.PHARMACIST)
public class PharmacistController
{
  final private int connectCount = 3;

  @Autowired private PharmacistService pharmacistService;

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.CREATE)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/add-info",
          method = RequestMethod.POST
  )
  public ResponseEntity<PHUserDTO> addUserInfo(@RequestBody PharmacistDTO pharmacistDTO)
  {
    return new ResponseEntity<PHUserDTO>(pharmacistService.saveUser(pharmacistDTO), HttpStatus.OK);
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.READ)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-all",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<UserDisplayDTO>> getAllPharmacist()
  {
    return new ResponseEntity<>(pharmacistService.findAllUsers(), HttpStatus.OK);
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.MANAGE)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/connect",
          method = RequestMethod.POST
  )
  public ResponseEntity connectPharmacist(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    ResponseEntity responseEntity= isEligibleToConnect();
    if(responseEntity.getStatusCode() ==HttpStatus.OK)
    {
      pharmacistService.connectWith(phUserConnectionDTO);
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return responseEntity;
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.READ)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-user-connections",
          method = RequestMethod.GET
  )
  public ResponseEntity<List<UserDisplayDTO>> getAllUserConnections()
  {
    List<UserDisplayDTO> users = pharmacistService.getAllUserConnections();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.MANAGE)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/disconnect",
          method = RequestMethod.PUT
  )
  public ResponseEntity disconnectPharmacist(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    pharmacistService.disconnectWith(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.READ)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/user-eligible-to-connect",
          method = RequestMethod.GET
  )
  public ResponseEntity isEligibleToConnect()
  {
    List<UserDisplayDTO> users = pharmacistService.getAllUserConnections();
    if (users.size() < connectCount)
    {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.CONFLICT);
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.VIEW_ALL)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/get-all-connections",
          method = RequestMethod.GET
  )
  public ResponseEntity getAllConnections()
  {
    return new ResponseEntity<>(pharmacistService.getAllConnections(), HttpStatus.OK);
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.MANAGE)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-connection-state",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateStatus(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    pharmacistService.updateState(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequiresPermission(resource = ResourceType.PHARMACIST, operation = OperationType.MANAGE)
  @RequestMapping(
          value = APIConstants.API_VERSION_V1 + "/update-connection-notes",
          method = RequestMethod.PUT
  )
  public ResponseEntity updateNotes(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
  {
    pharmacistService.updateNotes(phUserConnectionDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
