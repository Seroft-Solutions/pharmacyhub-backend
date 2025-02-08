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
import com.pharmacy.hub.entity.PharmacyManager;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import com.pharmacy.hub.entity.connections.PharmacyManagerConnections;
import com.pharmacy.hub.entity.connections.SalesmenConnections;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakAuthServiceImpl;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakGroupServiceImpl;
import com.pharmacy.hub.repository.*;
import com.pharmacy.hub.repository.connections.PharmacyManagerConnectionsRepository;
import com.pharmacy.hub.security.TenantContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PharmacyManagerService extends PHEngine implements PHUserService
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
  private PharmacyManagerConnectionsRepository pharmacyManagerConnectionsRepository;
  @Autowired
  private PHMapper phMapper;
  @Autowired
  private KeycloakGroupServiceImpl keycloakGroupServiceImpl;
  @Autowired
  private UserRepository userRepository;


  @Autowired
  private KeycloakAuthServiceImpl keycloakAuthServiceImpl;
  private final String realm;
  PharmacyManagerService(@org.springframework.beans.factory.annotation.Value("${keycloak.realm}") String realm){
    this.realm=realm;
  }


  @Override
  public PHUserDTO saveUser(PHUserDTO pharmacyManagerDTO)
  {
    User user= new User();
    String groupName="ADMIN";
    String groupId=keycloakGroupServiceImpl.findGroupIdByName(groupName);
    keycloakGroupServiceImpl.assignUserToGroup(TenantContext.getCurrentTenant(), groupId);
    PharmacyManager PharmacyManager = phMapper.getPharmacyManager((PharmacyManagerDTO) pharmacyManagerDTO);
    user.setId(TenantContext.getCurrentTenant());
    user.setRegistered(true);
    user.setOpenToConnect(true);
    userRepository.save(user);
    PharmacyManager.setUser(user);
//    getLoggedInUser().setRegistered(true);
//    getLoggedInUser().setUserType(UserEnum.PHARMACY_MANAGER.getUserEnum());
//    PharmacyManager.setUser(getLoggedInUser());
    PharmacyManager savedPharmacyManager = pharmacyManagerRepository.save(PharmacyManager);
    return phMapper.getPharmacyManagerDTO(savedPharmacyManager);
  }
  public void approveStatus(Long id)
  {
    PharmacyManager pharmacyManager = pharmacyManagerRepository.findById(id).orElseThrow(() -> new RuntimeException("PharmacyManager not found"));
    User requesterId = pharmacyManager.getUser();
    PharmacyManagerConnections pharmacyManagerConnection = pharmacyManagerConnectionsRepository.findByUserId(requesterId);
    pharmacyManagerConnection.setConnectionStatus(ConnectionStatusEnum.APPROVED);
    pharmacyManagerConnectionsRepository.save(pharmacyManagerConnection);
  }

  public void rejectStatus(Long id)
  {
    PharmacyManager pharmacyManager = pharmacyManagerRepository.findById(id).orElseThrow(() -> new RuntimeException("PharmacyManager not found"));
    User requesterId = pharmacyManager.getUser();
    PharmacyManagerConnections pharmacyManagerConnection = pharmacyManagerConnectionsRepository.findByUserId(requesterId);
    pharmacyManagerConnection.setConnectionStatus(ConnectionStatusEnum.REJECTED);
    pharmacyManagerConnectionsRepository.save(pharmacyManagerConnection);
  }

  @Override
  public PHUserDTO updateUser(PHUserDTO pharmacyManagerDTO)
  {
    PharmacyManager PharmacyManager = phMapper.getPharmacyManager((PharmacyManagerDTO) pharmacyManagerDTO);
    PharmacyManager.setUser(getLoggedInUser());
    PharmacyManager savedPharmacyManager = pharmacyManagerRepository.save(PharmacyManager);
    return phMapper.getPharmacyManagerDTO(savedPharmacyManager);
  }

  @Override
  public PHUserDTO findUser(long id)
  {
    Optional<PharmacyManager> pharmacyManager = pharmacyManagerRepository.findById(id);
    return phMapper.getPharmacyManagerDTO(pharmacyManager.get());
  }

  @Override
  public List<UserDisplayDTO> findAllUsers() {
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);

    // Find the PHARMACIST group
    Optional<GroupRepresentation> pharmacyManagerGroup = realmResource.groups().groups().stream()
                                                                 .filter(group -> "ADMIN".equalsIgnoreCase(group.getName()))
                                                                 .findFirst();

    if (pharmacyManagerGroup.isEmpty()) {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> pharmacyManagerUserIds = realmResource.groups().group(pharmacyManagerGroup.get().getId()).members().stream()
                                                  .map(UserRepresentation::getId)
                                                  .collect(Collectors.toList());

    return pharmacyManagerRepository.findAll().stream()
                               .filter(pharmacyManager -> pharmacyManagerUserIds.contains(pharmacyManager.getUser().getId()))
                               .map(pharmacyManager -> {
                                 UserRepresentation keycloakUser = realmResource.users().get(pharmacyManager.getUser().getId()).toRepresentation();
                                 UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(pharmacyManager.getUser());
                                 userDisplayDTO.setFirstName(keycloakUser.getFirstName());
                                 userDisplayDTO.setLastName(keycloakUser.getLastName());
                                 userDisplayDTO.setPharmacyManager(phMapper.getPharmacyManagerDTO(pharmacyManager));

                                 return userDisplayDTO;
                               }).collect(Collectors.toList());
  }

  public String getUserGroup(String userId)
  {
    return keycloakGroupServiceImpl.getAllUserGroups(userId).get(0).getName();
  }

  public void connectWith(PharmacyManagerConnectionsDTO pharmacyManagerConnectionsDTO)
  {
    pharmacyManagerConnectionsDTO.setUserId(TenantContext.getCurrentTenant());
    pharmacyManagerConnectionsDTO.setConnectionStatus(ConnectionStatusEnum.PENDING);
    pharmacyManagerConnectionsDTO.setNotes("User Want to connect");
    pharmacyManagerConnectionsDTO.setUserGroup(getUserGroup(TenantContext.getCurrentTenant()));
    PharmacyManagerConnections pharmacyManagerConnections = phMapper.getPharmacyManagerConnections(pharmacyManagerConnectionsDTO);
    pharmacyManagerConnectionsRepository.save(pharmacyManagerConnections);
  }

  @Override
  public List<UserDisplayDTO> getAllUserConnections()
  {
//
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);
    String currentUserId = TenantContext.getCurrentTenant();

    // Find the PHARMACIST group
    Optional<GroupRepresentation> pharmacyManagerGroup = realmResource.groups().groups().stream().filter(group -> "ADMIN".equalsIgnoreCase(group.getName())).findFirst();

    if (pharmacyManagerGroup.isEmpty())
    {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> pharmacyManagerUserIds = realmResource.groups().group(pharmacyManagerGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

    // Get current user's pharmacyManager record
    PharmacyManager currentUserPharmacyManager = pharmacyManagerRepository.findByUser_Id(currentUserId);
    if (currentUserPharmacyManager == null)
    {
      return Collections.emptyList();
    }

    // Find all pending connections for the current user's pharmacyManager ID
    List<PharmacyManagerConnections> pendingConnections = pharmacyManagerConnectionsRepository.findByPharmacyManagerIdAndConnectionStatus(currentUserPharmacyManager, ConnectionStatusEnum.APPROVED);

    // Map the connections to UserDisplayDTO
    return pendingConnections.stream().map(connection -> {
      // Get the requesting user's information
      User requestingUser = connection.getUserId();
      if (!pharmacyManagerUserIds.contains(requestingUser.getId()))
      {
        return null;
      }

      UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
      userDisplayDTO.setFirstName(keycloakUser.getFirstName());
      userDisplayDTO.setLastName(keycloakUser.getLastName());

      // Get the pharmacyManager details for the requesting user
      PharmacyManager requestingPharmacyManager = pharmacyManagerRepository.findByUser_Id(requestingUser.getId());
      userDisplayDTO.setPharmacyManager(phMapper.getPharmacyManagerDTO(requestingPharmacyManager));
      userDisplayDTO.setConnected(true); // Since we found a connection

      return userDisplayDTO;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }


  public List<UserDisplayDTO> findPendingUsers()
  {
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);
    String currentUserId = TenantContext.getCurrentTenant();

    // Find the PHARMACIST group
    Optional<GroupRepresentation> pharmacyManagerGroup = realmResource.groups().groups().stream().filter(group -> "ADMIN".equalsIgnoreCase(group.getName())).findFirst();

    if (pharmacyManagerGroup.isEmpty())
    {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> pharmacyManagerUserIds = realmResource.groups().group(pharmacyManagerGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

    // Get current user's pharmacyManager record
    PharmacyManager currentUserPharmacyManager = pharmacyManagerRepository.findByUser_Id(currentUserId);
    if (currentUserPharmacyManager == null)
    {
      return Collections.emptyList();
    }

    // Find all pending connections for the current user's pharmacyManager ID
    List<PharmacyManagerConnections> pendingConnections = pharmacyManagerConnectionsRepository.findByPharmacyManagerIdAndConnectionStatus(currentUserPharmacyManager, ConnectionStatusEnum.PENDING);

    // Map the connections to UserDisplayDTO
    return pendingConnections.stream().map(connection -> {
      // Get the requesting user's information
      User requestingUser = connection.getUserId();
      if (!pharmacyManagerUserIds.contains(requestingUser.getId()))
      {
        return null;
      }

      UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
      userDisplayDTO.setFirstName(keycloakUser.getFirstName());
      userDisplayDTO.setLastName(keycloakUser.getLastName());

      // Get the pharmacyManager details for the requesting user
      PharmacyManager requestingPharmacyManager = pharmacyManagerRepository.findByUser_Id(requestingUser.getId());
      userDisplayDTO.setPharmacyManager(phMapper.getPharmacyManagerDTO(requestingPharmacyManager));
      userDisplayDTO.setConnected(true); // Since we found a connection

      return userDisplayDTO;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }




  @Override
  public void updateState(PHUserConnectionDTO userConnectionDTO)
  {
//    PharmacyManagerConnections pharmacyManagerConnections = pharmacyManagerConnectionsRepository.findById(userConnectionDTO.getId()).get();
//    pharmacyManagerConnections.setState(userConnectionDTO.getState());
//    pharmacyManagerConnectionsRepository.save(pharmacyManagerConnections);
  }

  @Override
  public void updateNotes(PHUserConnectionDTO userConnectionDTO)
  {
    PharmacyManagerConnections pharmacyManagerConnections = pharmacyManagerConnectionsRepository.findById(userConnectionDTO.getId()).get();
    pharmacyManagerConnections.setNotes(userConnectionDTO.getNotes());
    pharmacyManagerConnectionsRepository.save(pharmacyManagerConnections);
  }

  @Override
  public List getAllConnections()
  {
    return pharmacyManagerConnectionsRepository.findAll();

  }

  @Override
  public void disconnectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
    }

  public PharmacyManager getPharmacyManager() {
    return pharmacyManagerRepository.findByUser(getLoggedInUser());
  }
}



