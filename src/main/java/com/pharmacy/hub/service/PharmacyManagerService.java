package com.pharmacy.hub.service;

import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.constants.UserEnum;
import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.PharmacyManagerDTO;
import com.pharmacy.hub.dto.display.ConnectionDisplayDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.engine.PHEngine;
import com.pharmacy.hub.engine.PHMapper;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.PharmacyManager;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacyManagerConnections;
import com.pharmacy.hub.entity.connections.SalesmenConnections;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakGroupServiceImpl;
import com.pharmacy.hub.repository.*;
import com.pharmacy.hub.repository.connections.PharmacyManagerConnectionsRepository;
import com.pharmacy.hub.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
  public List<UserDisplayDTO> findAllUsers()
  {
    return pharmacyManagerRepository.findAll().stream().map(pharmacyManager -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(pharmacyManager.getUser());
      userDisplayDTO.setPharmacyManager(phMapper.getPharmacyManagerDTO(pharmacyManager));
      //userDisplayDTO.setConnected(getAllUserConnections().stream().anyMatch(userDisplayDTO1 -> userDisplayDTO1.getPharmacyManager().getId().equals(pharmacyManager.getId())));

      return userDisplayDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public void connectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
    PharmacyManager pharmacyManager = phMapper.getPharmacyManager((PharmacyManagerDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<PharmacyManagerConnections> pharmacyManagerConnectionsList = pharmacyManagerConnectionsRepository.findByUserAndPharmacyManagerAndState(getLoggedInUser(), pharmacyManager, StateEnum.READY_TO_CONNECT);

    if (pharmacyManagerConnectionsList.isEmpty())
    {
      PharmacyManagerConnections pharmacyManagerConnections = new PharmacyManagerConnections();
      pharmacyManagerConnections.setPharmacyManager(pharmacyManager);
      pharmacyManagerConnections.setUser(getLoggedInUser());
      pharmacyManagerConnectionsRepository.save(pharmacyManagerConnections);
    }
  }

  @Override
  public List<UserDisplayDTO> getAllUserConnections()
  {
    List<PharmacyManagerConnections> pharmacyManagerConnectionsList = pharmacyManagerConnectionsRepository.findByUserAndState(getLoggedInUser(), StateEnum.READY_TO_CONNECT);

    return pharmacyManagerConnectionsList.stream().map(pharmacyManagerConnection -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(pharmacyManagerConnection.getPharmacyManager().getUser());
      userDisplayDTO.setPharmacyManager(phMapper.getPharmacyManagerDTO(pharmacyManagerConnection.getPharmacyManager()));
      return userDisplayDTO;
    }).collect(Collectors.toList());

  }

  @Override
  public void updateState(PHUserConnectionDTO userConnectionDTO)
  {
    PharmacyManagerConnections pharmacyManagerConnections = pharmacyManagerConnectionsRepository.findById(userConnectionDTO.getId()).get();
    pharmacyManagerConnections.setState(userConnectionDTO.getState());
    pharmacyManagerConnectionsRepository.save(pharmacyManagerConnections);
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
    List<PharmacyManagerConnections> pharmacyManagerConnections = pharmacyManagerConnectionsRepository.findAll();

    return pharmacyManagerConnections.stream().map(pharmacistsConnection -> {
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
  public void disconnectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
    PharmacyManager pharmacyManager = phMapper.getPharmacyManager((PharmacyManagerDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<PharmacyManagerConnections> pharmacyManagerConnectionsList = pharmacyManagerConnectionsRepository.findByUserAndPharmacyManagerAndState(getLoggedInUser(), pharmacyManager, StateEnum.READY_TO_CONNECT);

    PharmacyManagerConnections pharmacyManagerConnection = pharmacyManagerConnectionsList.stream().findFirst().get();
    pharmacyManagerConnection.setState(StateEnum.CLIENT_DISCONNECT);
    pharmacyManagerConnectionsRepository.save(pharmacyManagerConnection);
  }

  public PharmacyManager getPharmacyManager() {
    return pharmacyManagerRepository.findByUser(getLoggedInUser());
  }
}



