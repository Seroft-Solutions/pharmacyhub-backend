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
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import com.pharmacy.hub.entity.connections.PharmacyManagerConnections;
import com.pharmacy.hub.entity.connections.ProprietorsConnections;
import com.pharmacy.hub.entity.connections.SalesmenConnections;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakAuthServiceImpl;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakGroupServiceImpl;
import com.pharmacy.hub.repository.*;
import com.pharmacy.hub.repository.connections.ProprietorsConnectionsRepository;
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

/**
 * Service Implementation for managing {@link Proprietor}.
 */
@Service
public class ProprietorService extends PHEngine implements PHUserService
{
  private final Logger log = LoggerFactory.getLogger(ProprietorService.class);
  @Autowired
  private ProprietorsConnectionsRepository proprietorsConnectionsRepository;
  
  @Autowired
  private PharmacistRepository pharmacistRepository;
  @Autowired
  private SalesmanRepository salesmanRepository;
  @Autowired
  private ProprietorRepository proprietorRepository;
  @Autowired
  private PharmacyManagerRepository pharmacyManagerRepository;

  @Autowired
  private PHMapper phMapper;

  @Autowired
  private KeycloakGroupServiceImpl keycloakGroupServiceImpl;
  @Autowired
  private UserRepository userRepository;


  @Autowired
  private KeycloakAuthServiceImpl keycloakAuthServiceImpl;
  private final String realm;
    @Autowired
    private UserService userService;

  ProprietorService(@org.springframework.beans.factory.annotation.Value("${keycloak.realm}") String realm){
    this.realm=realm;
  }
  @Override
  public PHUserDTO saveUser(PHUserDTO proprietorDTO)
  {
    User user= new User();
    String groupName="PROPRIETOR";
    String groupId=keycloakGroupServiceImpl.findGroupIdByName(groupName);
    keycloakGroupServiceImpl.assignUserToGroup(TenantContext.getCurrentTenant(), groupId);
    Proprietor proprietor = phMapper.getProprietor((ProprietorDTO) proprietorDTO);
    user.setId(TenantContext.getCurrentTenant());
    user.setRegistered(true);
    user.setOpenToConnect(true);
    userRepository.save(user);
    proprietor.setUser(user);
//    getLoggedInUser().setRegistered(true);
//    proprietor.setUser(getLoggedInUser());
//    getLoggedInUser().setUserType(UserEnum.PROPRIETOR.getUserEnum());
    Proprietor savedProprietor = proprietorRepository.save(proprietor);
    return phMapper.getProprietorDTO(savedProprietor);
  }

  @Override
  public PHUserDTO updateUser(PHUserDTO proprietorDTO)
  {
    Proprietor proprietor = phMapper.getProprietor((ProprietorDTO) proprietorDTO);
    proprietor.setUser(getLoggedInUser());
    Proprietor savedProprietor = proprietorRepository.save(proprietor);
    return phMapper.getProprietorDTO(savedProprietor);
  }


  public void approveStatus(Long id)
  {
    Proprietor proprietor = proprietorRepository.findById(id).orElseThrow(() -> new RuntimeException("Proprietor not found"));
    User requesterId = proprietor.getUser();
    ProprietorsConnections proprietorConnections = proprietorsConnectionsRepository.findByUserId(requesterId);
    proprietorConnections.setConnectionStatus(ConnectionStatusEnum.APPROVED);
    proprietorsConnectionsRepository.save(proprietorConnections);
  }

  public void rejectStatus(Long id)
  {
    Proprietor proprietor = proprietorRepository.findById(id).orElseThrow(() -> new RuntimeException("Proprietor not found"));
    User requesterId = proprietor.getUser();
    ProprietorsConnections proprietorConnections = proprietorsConnectionsRepository.findByUserId(requesterId);
    proprietorConnections.setConnectionStatus(ConnectionStatusEnum.REJECTED);
    proprietorsConnectionsRepository.save(proprietorConnections);
  }



  @Override
  public PHUserDTO findUser(long id)
  {
    Optional<Proprietor> proprietor = proprietorRepository.findById(id);
    return phMapper.getProprietorDTO(proprietor.get());
  }

  @Override
  public List<UserDisplayDTO> findAllUsers() {
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);

    // Find the PHARMACIST group
    Optional<GroupRepresentation> proprietorGroup = realmResource.groups().groups().stream()
                                                                      .filter(group -> "PROPRIETOR".equalsIgnoreCase(group.getName()))
                                                                      .findFirst();

    if (proprietorGroup.isEmpty()) {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> proprietorUserIds = realmResource.groups().group(proprietorGroup.get().getId()).members().stream()
                                                       .map(UserRepresentation::getId)
                                                       .collect(Collectors.toList());

    return proprietorRepository.findAll().stream()
                                    .filter(proprietor -> proprietorUserIds.contains(proprietor.getUser().getId()))
                                    .map(proprietor -> {
                                      UserRepresentation keycloakUser = realmResource.users().get(proprietor.getUser().getId()).toRepresentation();
                                      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(proprietor.getUser());
                                      userDisplayDTO.setFirstName(keycloakUser.getFirstName());
                                      userDisplayDTO.setLastName(keycloakUser.getLastName());
                                      userDisplayDTO.setProprietor(phMapper.getProprietorDTO(proprietor));

                                      return userDisplayDTO;
                                    }).collect(Collectors.toList());
  }
  public void connectWith(ProprietorsConnectionsDTO proprietorsConnectionsDTO)
  {
    proprietorsConnectionsDTO.setUserId(TenantContext.getCurrentTenant());
    proprietorsConnectionsDTO.setConnectionStatus(ConnectionStatusEnum.PENDING);
    proprietorsConnectionsDTO.setNotes("User Want to connect");
    proprietorsConnectionsDTO.setUserGroup(userService.getUserGroup(TenantContext.getCurrentTenant()));
    ProprietorsConnections proprietorsConnections = phMapper.getProprietorConnections(proprietorsConnectionsDTO);
    proprietorsConnectionsRepository.save(proprietorsConnections);
  }


  public List<UserDisplayDTO> findPendingUsers()
  {
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);
    String currentUserId = TenantContext.getCurrentTenant();

    // Find the PHARMACIST group
    Optional<GroupRepresentation> proprietorGroup = realmResource.groups().groups().stream().filter(group -> "PROPRIETOR".equalsIgnoreCase(group.getName())).findFirst();

    if (proprietorGroup.isEmpty())
    {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> proprietorUserIds = realmResource.groups().group(proprietorGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

    // Get current user's proprietor record
    Proprietor currentUserProprietor = proprietorRepository.findByUser_Id(currentUserId);
    if (currentUserProprietor == null)
    {
      return Collections.emptyList();
    }

    // Find all pending connections for the current user's proprietor ID
    List<ProprietorsConnections> pendingConnections = proprietorsConnectionsRepository.findByProprietorIdAndConnectionStatus(currentUserProprietor, ConnectionStatusEnum.PENDING);

    // Map the connections to UserDisplayDTO
    return pendingConnections.stream().map(connection -> {
      // Get the requesting user's information
      User requestingUser = connection.getUserId();
      if (!proprietorUserIds.contains(requestingUser.getId()))
      {
        return null;
      }

      UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
      userDisplayDTO.setFirstName(keycloakUser.getFirstName());
      userDisplayDTO.setLastName(keycloakUser.getLastName());

      // Get the proprietor details for the requesting user
      Proprietor requestingProprietor = proprietorRepository.findByUser_Id(requestingUser.getId());
      userDisplayDTO.setProprietor(phMapper.getProprietorDTO(requestingProprietor));
      userDisplayDTO.setConnected(true); // Since we found a connection

      return userDisplayDTO;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }


  @Override
  public List<UserDisplayDTO> getAllUserConnections()
  {
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);
    String currentUserId = TenantContext.getCurrentTenant();

    // Find the PHARMACIST group
    Optional<GroupRepresentation> proprietorGroup = realmResource.groups().groups().stream().filter(group -> "PROPRIETOR".equalsIgnoreCase(group.getName())).findFirst();

    if (proprietorGroup.isEmpty())
    {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> proprietorUserIds = realmResource.groups().group(proprietorGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

    // Get current user's proprietor record
    Proprietor currentUserProprietor = proprietorRepository.findByUser_Id(currentUserId);
    if (currentUserProprietor == null)
    {
      return Collections.emptyList();
    }

    // Find all pending connections for the current user's proprietor ID
    List<ProprietorsConnections> pendingConnections = proprietorsConnectionsRepository.findByProprietorIdAndConnectionStatus(currentUserProprietor, ConnectionStatusEnum.APPROVED);

    // Map the connections to UserDisplayDTO
    return pendingConnections.stream().map(connection -> {
      // Get the requesting user's information
      User requestingUser = connection.getUserId();
      if (!proprietorUserIds.contains(requestingUser.getId()))
      {
        return null;
      }

      UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
      userDisplayDTO.setFirstName(keycloakUser.getFirstName());
      userDisplayDTO.setLastName(keycloakUser.getLastName());

      // Get the proprietor details for the requesting user
      Proprietor requestingProprietor = proprietorRepository.findByUser_Id(requestingUser.getId());
      userDisplayDTO.setProprietor(phMapper.getProprietorDTO(requestingProprietor));
      userDisplayDTO.setConnected(true); // Since we found a connection

      return userDisplayDTO;
    }).filter(Objects::nonNull).collect(Collectors.toList());

  }

  @Override
  public void updateState(PHUserConnectionDTO userConnectionDTO)
  {
    ProprietorsConnections proprietorsConnections = proprietorsConnectionsRepository.findById(userConnectionDTO.getId()).get();
//    proprietorsConnections.setState(userConnectionDTO.getState());
    proprietorsConnectionsRepository.save(proprietorsConnections);
  }

  @Override
  public List getAllConnections()
  {
    List<ProprietorsConnections> proprietorsConnections = proprietorsConnectionsRepository.findAll();

    return proprietorsConnections.stream().map(pharmacistsConnection -> {
      ConnectionDisplayDTO connectionDisplayDTO = phMapper.getConnectionDisplayDTO(pharmacistsConnection);

      connectionDisplayDTO.getUser().setPharmacist(null);
      connectionDisplayDTO.getUser().setSalesman(null);
      connectionDisplayDTO.getUser().setPharmacyManager(null);
      connectionDisplayDTO.getUser().setProprietor(null);
      
      if(connectionDisplayDTO.getUser().getUserType().equals(UserEnum.PHARMACIST.getUserEnum())){
        connectionDisplayDTO.getUser().setPharmacist(
                phMapper.getPharmacistDTO(
                        pharmacistRepository.findByUser(
                                phMapper.getUser(connectionDisplayDTO.getUser())
                        )
                )
        );
      }

      else if(connectionDisplayDTO.getUser().getUserType().equals(UserEnum.PROPRIETOR.getUserEnum())){
        connectionDisplayDTO.getUser().setProprietor(
                phMapper.getProprietorDTO(
                        proprietorRepository.findByUser(
                                phMapper.getUser(connectionDisplayDTO.getUser())
                        )
                )
        );
      }

      else if(connectionDisplayDTO.getUser().getUserType().equals(UserEnum.SALESMAN.getUserEnum())){
        connectionDisplayDTO.getUser().setSalesman(
                phMapper.getSalesmanDTO(
                        salesmanRepository.findByUser(
                                phMapper.getUser(connectionDisplayDTO.getUser())
                        )
                )
        );
      }

      else if(connectionDisplayDTO.getUser().getUserType().equals(UserEnum.PHARMACY_MANAGER.getUserEnum())){
        connectionDisplayDTO.getUser().setPharmacyManager(
                phMapper.getPharmacyManagerDTO(
                        pharmacyManagerRepository.findByUser(
                                phMapper.getUser(connectionDisplayDTO.getUser())
                        )
                )
        );
      }

      return connectionDisplayDTO;
    }).collect(Collectors.toList());


  }

  @Override
  public void updateNotes(PHUserConnectionDTO userConnectionDTO)
  {
    ProprietorsConnections proprietorsConnections = proprietorsConnectionsRepository.findById(userConnectionDTO.getId()).get();
    proprietorsConnections.setNotes(userConnectionDTO.getNotes());
    proprietorsConnectionsRepository.save(proprietorsConnections);
  }

  @Override
  public void disconnectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
//    Proprietor proprietor = phMapper.getProprietor((ProprietorDTO) findUser(phUserConnectionDTO.getConnectWith()));
//    List<ProprietorsConnections> proprietorConnectionsList = proprietorsConnectionsRepository.findByUserAndProprietorAndState(getLoggedInUser(), proprietor, StateEnum.READY_TO_CONNECT);
//
//    ProprietorsConnections proprietorConnection = proprietorConnectionsList.stream().findFirst().get();
////    proprietorConnection.setState(StateEnum.CLIENT_DISCONNECT);
//    proprietorsConnectionsRepository.save(proprietorConnection);
  }

  public Proprietor getProprietor() {
    return proprietorRepository.findByUser(getLoggedInUser());
  }
}

