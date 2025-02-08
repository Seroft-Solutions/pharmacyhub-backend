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
import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import com.pharmacy.hub.entity.connections.ProprietorsConnections;
import com.pharmacy.hub.entity.connections.SalesmenConnections;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakAuthServiceImpl;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakGroupServiceImpl;
import com.pharmacy.hub.repository.*;
import com.pharmacy.hub.repository.connections.SalesmenConnectionsRepository;
import com.pharmacy.hub.security.TenantContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalesmanService extends PHEngine implements PHUserService
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
  private SalesmenConnectionsRepository salesmenConnectionsRepository;

  @Autowired
  private KeycloakGroupServiceImpl keycloakGroupServiceImpl;
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PHMapper phMapper;

  @Autowired
  private KeycloakAuthServiceImpl keycloakAuthServiceImpl;
  private final String realm;
  SalesmanService(@org.springframework.beans.factory.annotation.Value("${keycloak.realm}") String realm){
    this.realm=realm;
  }

  @Override
  public PHUserDTO saveUser(PHUserDTO salesmanDTO)
  {
    User user= new User();
    String groupName="SALESMAN";
    String groupId=keycloakGroupServiceImpl.findGroupIdByName(groupName);
    keycloakGroupServiceImpl.assignUserToGroup(TenantContext.getCurrentTenant(), groupId);
    Salesman salesman = phMapper.getSalesman((SalesmanDTO) salesmanDTO);
    user.setId(TenantContext.getCurrentTenant());
    user.setRegistered(true);
    user.setOpenToConnect(true);
    userRepository.save(user);
    salesman.setUser(user);
    //    getLoggedInUser().setRegistered(true);
//    salesman.setUser(getLoggedInUser());
//    getLoggedInUser().setUserType(UserEnum.SALESMAN.getUserEnum());
    Salesman savedSalesman = salesmanRepository.save(salesman);
    return phMapper.getSalesmanDTO(savedSalesman);
  }

  @Override
  public PHUserDTO updateUser(PHUserDTO salesmanDTO)
  {
    Salesman salesman = phMapper.getSalesman((SalesmanDTO) salesmanDTO);
    salesman.setUser(getLoggedInUser());
    Salesman savedSalesman = salesmanRepository.save(salesman);
    return phMapper.getSalesmanDTO(savedSalesman);
  }

  public void approveStatus(Long id)
  {
    Salesman salesman = salesmanRepository.findById(id).orElseThrow(() -> new RuntimeException("Salesman not found"));
    User requesterId = salesman.getUser();
    SalesmenConnections salesmenConnections = salesmenConnectionsRepository.findByUserId(requesterId);
    salesmenConnections.setConnectionStatus(ConnectionStatusEnum.APPROVED);
    salesmenConnectionsRepository.save(salesmenConnections);
  }

  public void rejectStatus(Long id)
  {
    Salesman salesman = salesmanRepository.findById(id).orElseThrow(() -> new RuntimeException("Salesman not found"));
    User requesterId = salesman.getUser();
    SalesmenConnections salesmenConnections = salesmenConnectionsRepository.findByUserId(requesterId);
    salesmenConnections.setConnectionStatus(ConnectionStatusEnum.REJECTED);
    salesmenConnectionsRepository.save(salesmenConnections);
  }



  @Override
  public PHUserDTO findUser(long id)
  {
    Optional<Salesman> salesman = salesmanRepository.findById(id);
    return phMapper.getSalesmanDTO(salesman.get());
  }


  public void connectWith(SalesmenConnectionsDTO salesmanConnectionsDTO)
  {
    salesmanConnectionsDTO.setUserId(TenantContext.getCurrentTenant());
    salesmanConnectionsDTO.setConnectionStatus(ConnectionStatusEnum.PENDING);
    salesmanConnectionsDTO.setNotes("User Want to connect");
    salesmanConnectionsDTO.setUserGroup(getUserGroup(TenantContext.getCurrentTenant()));
    SalesmenConnections salesmanConnections = phMapper.getSalesmenConnections(salesmanConnectionsDTO);
    salesmenConnectionsRepository.save(salesmanConnections);
  }

  public String getUserGroup(String userId)
  {
    return keycloakGroupServiceImpl.getAllUserGroups(userId).get(0).getName();
  }
  @Override
  public List<UserDisplayDTO> findAllUsers() {
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);

    // Find the PHARMACIST group
    Optional<GroupRepresentation> salesmanGroup = realmResource.groups().groups().stream()
                                                                 .filter(group -> "SALESMAN".equalsIgnoreCase(group.getName()))
                                                                 .findFirst();

    if (salesmanGroup.isEmpty()) {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> salesmanUserIds = realmResource.groups().group(salesmanGroup.get().getId()).members().stream()
                                                  .map(UserRepresentation::getId)
                                                  .collect(Collectors.toList());

    return salesmanRepository.findAll().stream()
                               .filter(salesman -> salesmanUserIds.contains(salesman.getUser().getId()))
                               .map(salesman -> {
                                 UserRepresentation keycloakUser = realmResource.users().get(salesman.getUser().getId()).toRepresentation();
                                 UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(salesman.getUser());
                                 userDisplayDTO.setFirstName(keycloakUser.getFirstName());
                                 userDisplayDTO.setLastName(keycloakUser.getLastName());
                                 userDisplayDTO.setSalesman(phMapper.getSalesmanDTO(salesman));

                                 return userDisplayDTO;
                               }).collect(Collectors.toList());
  }


  public List<UserDisplayDTO> findPendingUsers()
  {
    Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);
    String currentUserId = TenantContext.getCurrentTenant();

    // Find the PHARMACIST group
    Optional<GroupRepresentation> salesmanGroup = realmResource.groups().groups().stream().filter(group -> "SALESMAN".equalsIgnoreCase(group.getName())).findFirst();

    if (salesmanGroup.isEmpty())
    {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> salesmenUserIds = realmResource.groups().group(salesmanGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

    // Get current user's pharmacist record
    Salesman currentUserSalesman = salesmanRepository.findByUser_Id(currentUserId);
    if (currentUserSalesman == null)
    {
      return Collections.emptyList();
    }

    // Find all pending connections for the current user's pharmacist ID
    List<SalesmenConnections> pendingConnections = salesmenConnectionsRepository.findBySalesmanIdAndConnectionStatus(currentUserSalesman, ConnectionStatusEnum.PENDING);

    // Map the connections to UserDisplayDTO
    return pendingConnections.stream().map(connection -> {
      // Get the requesting user's information
      User requestingUser = connection.getUserId();
      if (!salesmenUserIds.contains(requestingUser.getId()))
      {
        return null;
      }

      UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
      userDisplayDTO.setFirstName(keycloakUser.getFirstName());
      userDisplayDTO.setLastName(keycloakUser.getLastName());

      // Get the pharmacist details for the requesting user
      Salesman requestingSalesman = salesmanRepository.findByUser_Id(requestingUser.getId());
      userDisplayDTO.setSalesman(phMapper.getSalesmanDTO(requestingSalesman));
      userDisplayDTO.setConnected(true); // Since we found a connection

      return userDisplayDTO;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }



  @Override
  public List<UserDisplayDTO> getAllUserConnections()
  {Keycloak keycloak = keycloakAuthServiceImpl.getKeycloakInstance();
    RealmResource realmResource = keycloak.realm(realm);
    String currentUserId = TenantContext.getCurrentTenant();

    // Find the PHARMACIST group
    Optional<GroupRepresentation> salesmanGroup = realmResource.groups().groups().stream().filter(group -> "SALESMAN".equalsIgnoreCase(group.getName())).findFirst();

    if (salesmanGroup.isEmpty())
    {
      return Collections.emptyList();
    }

    // Get user IDs in the PHARMACIST group
    List<String> salesmenUserIds = realmResource.groups().group(salesmanGroup.get().getId()).members().stream().map(UserRepresentation::getId).collect(Collectors.toList());

    // Get current user's pharmacist record
    Salesman currentUserSalesman = salesmanRepository.findByUser_Id(currentUserId);
    if (currentUserSalesman == null)
    {
      return Collections.emptyList();
    }

    // Find all pending connections for the current user's pharmacist ID
    List<SalesmenConnections> pendingConnections = salesmenConnectionsRepository.findBySalesmanIdAndConnectionStatus(currentUserSalesman, ConnectionStatusEnum.APPROVED);

    // Map the connections to UserDisplayDTO
    return pendingConnections.stream().map(connection -> {
      // Get the requesting user's information
      User requestingUser = connection.getUserId();
      if (!salesmenUserIds.contains(requestingUser.getId()))
      {
        return null;
      }

      UserRepresentation keycloakUser = realmResource.users().get(requestingUser.getId()).toRepresentation();
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(requestingUser);
      userDisplayDTO.setFirstName(keycloakUser.getFirstName());
      userDisplayDTO.setLastName(keycloakUser.getLastName());

      // Get the pharmacist details for the requesting user
      Salesman requestingSalesman = salesmanRepository.findByUser_Id(requestingUser.getId());
      userDisplayDTO.setSalesman(phMapper.getSalesmanDTO(requestingSalesman));
      userDisplayDTO.setConnected(true); // Since we found a connection

      return userDisplayDTO;
    }).filter(Objects::nonNull).collect(Collectors.toList());

  }

  @Override
  public List getAllConnections()
  {
    List<SalesmenConnections> salesmenConnections = salesmenConnectionsRepository.findAll();

    return salesmenConnections.stream().map(pharmacistsConnection -> {
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
  public void updateState(PHUserConnectionDTO userConnectionDTO)
  {
    SalesmenConnections salesmenConnections = salesmenConnectionsRepository.findById(userConnectionDTO.getId()).get();
//    salesmenConnections.setState(userConnectionDTO.getState());
    salesmenConnectionsRepository.save(salesmenConnections);
  }

  @Override
  public void updateNotes(PHUserConnectionDTO userConnectionDTO)
  {
    SalesmenConnections salesmenConnections = salesmenConnectionsRepository.findById(userConnectionDTO.getId()).get();
    salesmenConnections.setNotes(userConnectionDTO.getNotes());
    salesmenConnectionsRepository.save(salesmenConnections);
  }

  @Override
  public void disconnectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
//    Salesman salesman = phMapper.getSalesman((SalesmanDTO) findUser(phUserConnectionDTO.getConnectWith()));
//    List<SalesmenConnections> salesmanConnectionsList = salesmenConnectionsRepository.findByUserAndSalesmanAndState(getLoggedInUser(), salesman, StateEnum.READY_TO_CONNECT);
//
//    SalesmenConnections salesmanConnection = salesmanConnectionsList.stream().findFirst().get();
////    salesmanConnection.setState(StateEnum.CLIENT_DISCONNECT);
//    salesmenConnectionsRepository.save(salesmanConnection);
  }
  
  public Salesman getSalesman(){
    return salesmanRepository.findByUser(getLoggedInUser());
  }
}



