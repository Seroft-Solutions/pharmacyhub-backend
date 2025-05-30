package com.pharmacyhub.service;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.constants.UserEnum;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.display.ConnectionDisplayDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.engine.PHMapper;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.connections.PharmacistsConnections;
import com.pharmacyhub.repository.PharmacistRepository;
import com.pharmacyhub.repository.PharmacyManagerRepository;
import com.pharmacyhub.repository.ProprietorRepository;
import com.pharmacyhub.repository.SalesmanRepository;
import com.pharmacyhub.repository.connections.PharmacistsConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

  @Override
  public PHUserDTO saveUser(PHUserDTO pharmacistDTO)
  {
    Pharmacist pharmacist = phMapper.getPharmacist((PharmacistDTO) pharmacistDTO);
    getLoggedInUser().setRegistered(true);
    getLoggedInUser().setUserType(com.pharmacyhub.entity.enums.UserType.PHARMACIST);
    pharmacist.setUser(getLoggedInUser());
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

  @Override
  public List<UserDisplayDTO> findAllUsers()
  {
    return pharmacistRepository.findAll().stream().map(pharmacist -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(pharmacist.getUser());
      userDisplayDTO.setPharmacist(phMapper.getPharmacistDTO(pharmacist));

      userDisplayDTO.setConnected(getAllUserConnections().stream().anyMatch(userDisplayDTO1 -> {
        return userDisplayDTO1.getPharmacist().getId().equals(pharmacist.getId());
      }));

      return userDisplayDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public void connectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
    Pharmacist pharmacist = phMapper.getPharmacist((PharmacistDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<PharmacistsConnections> pharmacistsConnectionsList = pharmacistsConnectionsRepository.findByUserAndPharmacistAndState(getLoggedInUser(), pharmacist, StateEnum.READY_TO_CONNECT);

    if (pharmacistsConnectionsList.isEmpty())
    {
      PharmacistsConnections pharmacistsConnections = new PharmacistsConnections();
      pharmacistsConnections.setPharmacist(pharmacist);
      pharmacistsConnections.setUser(getLoggedInUser());
      pharmacistsConnectionsRepository.save(pharmacistsConnections);
    }
  }

  @Override
  public List<UserDisplayDTO> getAllUserConnections()
  {
    List<PharmacistsConnections> pharmacistsConnectionsList = pharmacistsConnectionsRepository.findByUserAndState(getLoggedInUser(), StateEnum.READY_TO_CONNECT);

    return pharmacistsConnectionsList.stream().map(pharmacistsConnection -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(pharmacistsConnection.getPharmacist().getUser());
      userDisplayDTO.setPharmacist(phMapper.getPharmacistDTO(pharmacistsConnection.getPharmacist()));
      return userDisplayDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public void updateState(PHUserConnectionDTO userConnectionDTO)
  {
    PharmacistsConnections pharmacistsConnections = pharmacistsConnectionsRepository.findById(userConnectionDTO.getId()).get();
    pharmacistsConnections.setState(userConnectionDTO.getState());
    pharmacistsConnectionsRepository.save(pharmacistsConnections);
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
    Pharmacist pharmacist = phMapper.getPharmacist((PharmacistDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<PharmacistsConnections> pharmacistsConnectionsList = pharmacistsConnectionsRepository.findByUserAndPharmacistAndState(getLoggedInUser(), pharmacist, StateEnum.READY_TO_CONNECT);

    PharmacistsConnections pharmacistsConnection = pharmacistsConnectionsList.stream().findFirst().get();
    pharmacistsConnection.setState(StateEnum.CLIENT_DISCONNECT);
    pharmacistsConnectionsRepository.save(pharmacistsConnection);
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

  public Pharmacist getPharmacist() {
    return pharmacistRepository.findByUser(getLoggedInUser());
  }
}
