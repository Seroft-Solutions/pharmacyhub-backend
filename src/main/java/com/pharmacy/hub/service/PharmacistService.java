package com.pharmacy.hub.service;

import com.pharmacy.hub.constants.ConnectionStatusEnum;
import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.constants.UserEnum;
import com.pharmacy.hub.dto.*;
import com.pharmacy.hub.dto.display.ConnectionDisplayDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.engine.PHEngine;
import com.pharmacy.hub.engine.PHMapper;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakAuthServiceImpl;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakGroupServiceImpl;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakUserServiceImpl;
import com.pharmacy.hub.keycloak.services.KeycloakAuthService;
import com.pharmacy.hub.keycloak.utils.KeycloakGroupUtils;
import com.pharmacy.hub.keycloak.utils.KeycloakUtils;
import com.pharmacy.hub.repository.*;
import com.pharmacy.hub.repository.connections.PharmacistsConnectionsRepository;
import com.pharmacy.hub.security.TenantContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PharmacistService extends PHEngine implements PHUserService
{
    private final Logger log = LoggerFactory.getLogger(PharmacistService.class);

    @Autowired
    private PharmacistRepository pharmacistRepository;
    @Autowired
    private SalesmanRepository salesmanRepository;
    @Autowired
    private ProprietorRepository proprietorRepository;
    @Autowired
    private PharmacyManagerRepository pharmacyManagerRepository;

    @Autowired
    private PharmacistsConnectionsRepository pharmacistsConnectionsRepository;
    @Autowired
    private PHMapper phMapper;
    @Autowired
    private KeycloakGroupServiceImpl keycloakGroupServiceImpl;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakUserServiceImpl keycloakUserServiceImpl;
    @Autowired
    private KeycloakAuthServiceImpl keycloakAuthServiceImpl;
    private final String realm;
    @Autowired
    private UserService userService;

    PharmacistService(@org.springframework.beans.factory.annotation.Value("${keycloak.realm}") String realm)
    {
        this.realm = realm;
    }

    public void approveStatus(Long id)
    {
        Pharmacist pharmacist = pharmacistRepository.findById(id).orElseThrow(() -> new RuntimeException("Pharmacist not found"));
        User requesterId = pharmacist.getUser();
        PharmacistsConnections pharamacistConnection = pharmacistsConnectionsRepository.findByUserId(requesterId);
        pharamacistConnection.setConnectionStatus(ConnectionStatusEnum.APPROVED);
        pharmacistsConnectionsRepository.save(pharamacistConnection);
    }

    public void rejectStatus(Long id)
    {
        Pharmacist pharmacist = pharmacistRepository.findById(id).orElseThrow(() -> new RuntimeException("Pharmacist not found"));
        User requesterId = pharmacist.getUser();
        PharmacistsConnections pharamacistConnection = pharmacistsConnectionsRepository.findByUserId(requesterId);
        pharamacistConnection.setConnectionStatus(ConnectionStatusEnum.REJECTED);
        pharmacistsConnectionsRepository.save(pharamacistConnection);
    }


    @Override
    public PHUserDTO saveUser(PHUserDTO pharmacistDTO)
    {
        User user = new User();
        String groupName = "PHARMACIST";
        String groupId = keycloakGroupServiceImpl.findGroupIdByName(groupName);
        keycloakGroupServiceImpl.assignUserToGroup(TenantContext.getCurrentTenant(), groupId);
        Pharmacist pharmacist = phMapper.getPharmacist((PharmacistDTO) pharmacistDTO);
        user.setId(TenantContext.getCurrentTenant());
        user.setRegistered(true);
        user.setOpenToConnect(true);
        userRepository.save(user);
        pharmacist.setUser(user);

        //pharmacist.setUser(userId);
        //    getLoggedInUser().setRegistered(true);
        //    getLoggedInUser().setUserType(UserEnum.PHARMACIST.getUserEnum());
        //    pharmacist.setUser(getLoggedInUser());
        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        return phMapper.getPharmacistDTO(savedPharmacist);
    }

    @Override
    public PHUserDTO updateUser(PHUserDTO proprietorDTO)
    {
        Pharmacist pharmacist = phMapper.getPharmacist((PharmacistDTO) proprietorDTO);
        pharmacist.setUser(getLoggedInUser());
        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        return phMapper.getPharmacistDTO(savedPharmacist);
    }

    @Override
    public PHUserDTO findUser(long id)
    {
        Optional<Pharmacist> pharmacist = pharmacistRepository.findById(id);
        return phMapper.getPharmacistDTO(pharmacist.get());
    }


    public List<UserDisplayDTO> findPendingUsers()
    {
        Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        String currentUserId = TenantContext.getCurrentTenant();

        // Find the PHARMACIST group
        Optional<GroupRepresentation> pharmacistGroup = realmResource.groups().groups().stream().filter(group -> "PHARMACIST".equalsIgnoreCase(group.getName())).findFirst();

        if (pharmacistGroup.isEmpty())
        {
            return Collections.emptyList();
        }

        // Get user IDs in the PHARMACIST group
        List<String> pharmacistUserIds = realmResource.groups().group(pharmacistGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

        // Get current user's pharmacist record
        Pharmacist currentUserPharmacist = pharmacistRepository.findByUser_Id(currentUserId);
        if (currentUserPharmacist == null)
        {
            return Collections.emptyList();
        }

        // Find all pending connections for the current user's pharmacist ID
        List<PharmacistsConnections> pendingConnections = pharmacistsConnectionsRepository.findByPharmacistIdAndConnectionStatus(currentUserPharmacist, ConnectionStatusEnum.PENDING);

        // Map the connections to UserDisplayDTO
        return pendingConnections.stream().map(connection -> {
            // Get the requesting user's information
            User requestingUser = connection.getUserId();
            if (!pharmacistUserIds.contains(requestingUser.getId()))
            {
                return null;
            }

            UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
            UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
            userDisplayDTO.setFirstName(keycloakUser.getFirstName());
            userDisplayDTO.setLastName(keycloakUser.getLastName());

            // Get the pharmacist details for the requesting user
            Pharmacist requestingPharmacist = pharmacistRepository.findByUser_Id(requestingUser.getId());
            userDisplayDTO.setPharmacist(phMapper.getPharmacistDTO(requestingPharmacist));
            userDisplayDTO.setConnected(true); // Since we found a connection

            return userDisplayDTO;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    @Override
    public List<UserDisplayDTO> findAllUsers()
    {
        Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);

        // Find the PHARMACIST group
        Optional<GroupRepresentation> pharmacistGroup = realmResource.groups().groups().stream().filter(group -> "PHARMACIST".equalsIgnoreCase(group.getName())).findFirst();

        if (pharmacistGroup.isEmpty())
        {
            return Collections.emptyList();
        }

        // Get user IDs in the PHARMACIST group
        List<String> pharmacistUserIds = realmResource.groups().group(pharmacistGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

        return pharmacistRepository.findAll().stream().filter(pharmacist -> pharmacistUserIds.contains(pharmacist.getUser().getId())).map(pharmacist -> {
            UserRepresentation keycloakUser = realmResource.users().get(pharmacist.getUser().getId()).toRepresentation();
            UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(pharmacist.getUser());
            userDisplayDTO.setFirstName(keycloakUser.getFirstName());
            userDisplayDTO.setLastName(keycloakUser.getLastName());
            userDisplayDTO.setPharmacist(phMapper.getPharmacistDTO(pharmacist));

            return userDisplayDTO;
        }).collect(Collectors.toList());
    }




    public void connectWith2(PharmacistsConnectionsDTO pharmacistsConnectionsDTO)
    {
        pharmacistsConnectionsDTO.setUserId(TenantContext.getCurrentTenant());
        pharmacistsConnectionsDTO.setConnectionStatus(ConnectionStatusEnum.PENDING);
        pharmacistsConnectionsDTO.setNotes("User Want to connect");
        pharmacistsConnectionsDTO.setUserGroup(userService.getUserGroup(TenantContext.getCurrentTenant()));
        PharmacistsConnections pharmacistsConnections = phMapper.getPharmacistsConnections(pharmacistsConnectionsDTO);
        pharmacistsConnectionsRepository.save(pharmacistsConnections);
    }


    public void connectionRequests()
    {

    }


    @Override
    public List<UserDisplayDTO> getAllUserConnections()
    {
        Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        String currentUserId = TenantContext.getCurrentTenant();

        // Find the PHARMACIST group
        Optional<GroupRepresentation> pharmacistGroup = realmResource.groups().groups().stream().filter(group -> "PHARMACIST".equalsIgnoreCase(group.getName())).findFirst();

        if (pharmacistGroup.isEmpty())
        {
            return Collections.emptyList();
        }

        // Get user IDs in the PHARMACIST group
        List<String> pharmacistUserIds = realmResource.groups().group(pharmacistGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

        // Get current user's pharmacist record
        Pharmacist currentUserPharmacist = pharmacistRepository.findByUser_Id(currentUserId);
        if (currentUserPharmacist == null)
        {
            return Collections.emptyList();
        }

        // Find all approved connections for the current user's pharmacist ID
        List<PharmacistsConnections> approvedConnections = pharmacistsConnectionsRepository.findByPharmacistIdAndConnectionStatus(currentUserPharmacist, ConnectionStatusEnum.APPROVED);

        // Map the connections to UserDisplayDTO
        return approvedConnections.stream().map(connection -> {
            // Get the requesting user's information
            User requestingUser = connection.getUserId();
            if (!pharmacistUserIds.contains(requestingUser.getId()))
            {
                return null;
            }

            UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
            UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
            userDisplayDTO.setFirstName(keycloakUser.getFirstName());
            userDisplayDTO.setLastName(keycloakUser.getLastName());

            // Get the pharmacist details for the requesting user
            Pharmacist connectedPharmacist = pharmacistRepository.findByUser_Id(requestingUser.getId());
            userDisplayDTO.setPharmacist(phMapper.getPharmacistDTO(connectedPharmacist));

            return userDisplayDTO;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    @Override
    public void updateState(PHUserConnectionDTO userConnectionDTO)
    {
        //    PharmacistsConnections pharmacistsConnections = pharmacistsConnectionsRepository.findById(userConnectionDTO.getId()).get();
        //    pharmacistsConnections.setState(userConnectionDTO.getState());
        //    pharmacistsConnectionsRepository.save(pharmacistsConnections);
    }

    @Override
    public void updateNotes(PHUserConnectionDTO userConnectionDTO)
    {
        PharmacistsConnections pharmacistsConnections = pharmacistsConnectionsRepository.findById(userConnectionDTO.getId()).get();
        pharmacistsConnections.setNotes(userConnectionDTO.getNotes());
        pharmacistsConnectionsRepository.save(pharmacistsConnections);
    }

    @Override
    public void disconnectWith(PHUserConnectionDTO phUserConnectionDTO)
    {
        //    Pharmacist pharmacist = phMapper.getPharmacist((PharmacistDTO) findUser(phUserConnectionDTO.getConnectWith()));
        ////    List<PharmacistsConnections> pharmacistsConnectionsList = pharmacistsConnectionsRepository.findByUserAndPharmacistAndState(getLoggedInUser(), pharmacist, StateEnum.READY_TO_CONNECT);
        //
        ////    PharmacistsConnections pharmacistsConnection = pharmacistsConnectionsList.stream().findFirst().get();
        //    pharmacistsConnection.setState(StateEnum.CLIENT_DISCONNECT);
        //    pharmacistsConnectionsRepository.save(pharmacistsConnection);
    }

    @Override
    public List getAllConnections()
    {
        List<PharmacistsConnections> pharmacistsConnectionsList = pharmacistsConnectionsRepository.findAll();

        return pharmacistsConnectionsList.stream().map(pharmacistsConnection -> {
            ConnectionDisplayDTO connectionDisplayDTO = phMapper.getConnectionDisplayDTO(pharmacistsConnection);

            connectionDisplayDTO.getUser().setPharmacist(null);
            connectionDisplayDTO.getUser().setSalesman(null);
            connectionDisplayDTO.getUser().setPharmacyManager(null);
            connectionDisplayDTO.getUser().setProprietor(null);

            if (connectionDisplayDTO.getUser().getUserType().equals(UserEnum.PHARMACIST.getUserEnum()))
            {
                connectionDisplayDTO.getUser().setPharmacist(phMapper.getPharmacistDTO(pharmacistRepository.findByUser(phMapper.getUser(connectionDisplayDTO.getUser()))));
            }

            else if (connectionDisplayDTO.getUser().getUserType().equals(UserEnum.PROPRIETOR.getUserEnum()))
            {
                connectionDisplayDTO.getUser().setProprietor(phMapper.getProprietorDTO(proprietorRepository.findByUser(phMapper.getUser(connectionDisplayDTO.getUser()))));
            }

            else if (connectionDisplayDTO.getUser().getUserType().equals(UserEnum.SALESMAN.getUserEnum()))
            {
                connectionDisplayDTO.getUser().setSalesman(phMapper.getSalesmanDTO(salesmanRepository.findByUser(phMapper.getUser(connectionDisplayDTO.getUser()))));
            }

            else if (connectionDisplayDTO.getUser().getUserType().equals(UserEnum.PHARMACY_MANAGER.getUserEnum()))
            {
                connectionDisplayDTO.getUser().setPharmacyManager(phMapper.getPharmacyManagerDTO(pharmacyManagerRepository.findByUser(phMapper.getUser(connectionDisplayDTO.getUser()))));
            }

            return connectionDisplayDTO;
        }).collect(Collectors.toList());

    }

}



