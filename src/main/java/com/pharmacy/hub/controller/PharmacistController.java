package com.pharmacy.hub.controller;

import com.pharmacy.hub.constants.APIConstants;
import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.PharmacistDTO;
import com.pharmacy.hub.dto.PharmacistsConnectionsDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakGroupServiceImpl;
import com.pharmacy.hub.service.PharmacistService;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(APIConstants.BASE_MAPPING + APIConstants.PHARMACIST)
public class PharmacistController
{
    final private int connectCount = 3;

    @Autowired
    private PharmacistService pharmacistService;
    @Autowired
    private KeycloakGroupServiceImpl keycloakGroupServiceImpl;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/add-info", method = RequestMethod.POST)
    public ResponseEntity<PHUserDTO> addUserInfo(@RequestBody PharmacistDTO pharmacistDTO)
    {
        return new ResponseEntity<PHUserDTO>(pharmacistService.saveUser(pharmacistDTO), HttpStatus.OK);
    }

    //@PreAuthorize("isAuthenticated()")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all", method = RequestMethod.GET)
    public ResponseEntity<List<UserDisplayDTO>> getAllPharmacist()
    {
        return new ResponseEntity<>(pharmacistService.findAllUsers(), HttpStatus.OK);
    }


//    @PreAuthorize("isAuthenticated()")
@GetMapping(value = APIConstants.API_VERSION_V1 + "/CheckUserGroup/{id}")
public ResponseEntity<List<GroupRepresentation>> checkUserGroup(@PathVariable Long id) {
    List<GroupRepresentation> usersGroup = keycloakGroupServiceImpl.getAllUserGroups(id.toString());
    if (usersGroup != null && !usersGroup.isEmpty()) {
        return ResponseEntity.ok(usersGroup); // Use OK (200) for successful responses
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Not found response
}
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all-pending-requests", method = RequestMethod.GET)
    public ResponseEntity<List<UserDisplayDTO>> getAllPendingRequests()
    {
        return new ResponseEntity<>(pharmacistService.findPendingUsers(), HttpStatus.OK);
    }


    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all-connections", method = RequestMethod.GET)
    public ResponseEntity<List<UserDisplayDTO>> getAllConnections()
    {
        return new ResponseEntity<>(pharmacistService.getAllUserConnections(), HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/connect", method = RequestMethod.POST)
    public ResponseEntity connectPharmacist(@RequestBody PharmacistsConnectionsDTO pharmacistsConnectionsDTO)
    {
            //ResponseEntity responseEntity = isEligibleToConnect();
//            if (responseEntity.getStatusCode() == HttpStatus.OK)
//            {
                pharmacistService.connectWith2(pharmacistsConnectionsDTO);
                return new ResponseEntity<>(HttpStatus.OK);
//            }
           // return responseEntity;
    }


    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/approveStatus/{id}", method = RequestMethod.POST)
    public ResponseEntity approveStatus(@PathVariable Long id) {
        pharmacistService.approveStatus(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/rejectStatus/{id}", method = RequestMethod.POST)
    public ResponseEntity rejectStatus(@PathVariable Long id)
    {

        pharmacistService.rejectStatus(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-user-connections", method = RequestMethod.GET)
    public ResponseEntity<List<UserDisplayDTO>> getAllUserConnections()
    {
        List<UserDisplayDTO> users = pharmacistService.getAllUserConnections();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/disconnect", method = RequestMethod.PUT)
    public ResponseEntity disconnectPharmacist(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
    {
        pharmacistService.disconnectWith(phUserConnectionDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/user-eligible-to-connect", method = RequestMethod.GET)
    public ResponseEntity isEligibleToConnect()
    {
        List<UserDisplayDTO> users = pharmacistService.getAllUserConnections();
        if (users.size() < connectCount)
        {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/get-all-connections", method = RequestMethod.GET)
//    public ResponseEntity getAllConnections()
//    {
//        return new ResponseEntity<>(pharmacistService.getAllConnections(), HttpStatus.OK);
//    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/update-connection-state", method = RequestMethod.PUT)
    public ResponseEntity updateStatus(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
    {
        pharmacistService.updateState(phUserConnectionDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @RequestMapping(value = APIConstants.API_VERSION_V1 + "/update-connection-notes", method = RequestMethod.PUT)
    public ResponseEntity updateNotes(@RequestBody PHUserConnectionDTO phUserConnectionDTO)
    {
        pharmacistService.updateNotes(phUserConnectionDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}





