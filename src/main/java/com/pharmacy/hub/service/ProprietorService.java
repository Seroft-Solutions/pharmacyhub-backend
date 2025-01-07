package com.pharmacy.hub.service;

import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.constants.UserEnum;
import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.ProprietorDTO;
import com.pharmacy.hub.dto.display.ConnectionDisplayDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.engine.PHEngine;
import com.pharmacy.hub.engine.PHMapper;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.connections.PharmacyManagerConnections;
import com.pharmacy.hub.entity.connections.ProprietorsConnections;
import com.pharmacy.hub.repository.PharmacistRepository;
import com.pharmacy.hub.repository.PharmacyManagerRepository;
import com.pharmacy.hub.repository.ProprietorRepository;
import com.pharmacy.hub.repository.SalesmanRepository;
import com.pharmacy.hub.repository.connections.ProprietorsConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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


  @Override
  public PHUserDTO saveUser(PHUserDTO proprietorDTO)
  {
    Proprietor proprietor = phMapper.getProprietor((ProprietorDTO) proprietorDTO);
    getLoggedInUser().setRegistered(true);
    proprietor.setUser(getLoggedInUser());
    getLoggedInUser().setUserType(UserEnum.PROPRIETOR.getUserEnum());
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

  @Override
  public PHUserDTO findUser(long id)
  {
    Optional<Proprietor> proprietor = proprietorRepository.findById(id);
    return phMapper.getProprietorDTO(proprietor.get());
  }

  @Override
  public List<UserDisplayDTO> findAllUsers()
  {
    return proprietorRepository.findAll().stream().map(proprietor -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(proprietor.getUser());
      userDisplayDTO.setProprietor(phMapper.getProprietorDTO(proprietor));

      //userDisplayDTO.setConnected(getAllUserConnections().stream().anyMatch(userDisplayDTO1 -> userDisplayDTO1.getProprietor().getId().equals(proprietor.getId())));

      return userDisplayDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public void connectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
    Proprietor proprietor = phMapper.getProprietor((ProprietorDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<ProprietorsConnections> proprietorConnectionsList = proprietorsConnectionsRepository.findByUserAndProprietorAndState(getLoggedInUser(), proprietor, StateEnum.READY_TO_CONNECT);

    if (proprietorConnectionsList.isEmpty())
    {
      ProprietorsConnections proprietorConnections = new ProprietorsConnections();
      proprietorConnections.setProprietor(proprietor);
      proprietorConnections.setUser(getLoggedInUser());
      proprietorsConnectionsRepository.save(proprietorConnections);
    }
  }

  @Override
  public List<UserDisplayDTO> getAllUserConnections()
  {
    List<ProprietorsConnections> proprietorConnectionsList = proprietorsConnectionsRepository.findByUserAndState(getLoggedInUser(), StateEnum.READY_TO_CONNECT);

    return proprietorConnectionsList.stream().map(proprietorConnection -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(proprietorConnection.getProprietor().getUser());
      userDisplayDTO.setProprietor(phMapper.getProprietorDTO(proprietorConnection.getProprietor()));
      return userDisplayDTO;
    }).collect(Collectors.toList());

  }

  @Override
  public void updateState(PHUserConnectionDTO userConnectionDTO)
  {
    ProprietorsConnections proprietorsConnections = proprietorsConnectionsRepository.findById(userConnectionDTO.getId()).get();
    proprietorsConnections.setState(userConnectionDTO.getState());
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
    Proprietor proprietor = phMapper.getProprietor((ProprietorDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<ProprietorsConnections> proprietorConnectionsList = proprietorsConnectionsRepository.findByUserAndProprietorAndState(getLoggedInUser(), proprietor, StateEnum.READY_TO_CONNECT);

    ProprietorsConnections proprietorConnection = proprietorConnectionsList.stream().findFirst().get();
    proprietorConnection.setState(StateEnum.CLIENT_DISCONNECT);
    proprietorsConnectionsRepository.save(proprietorConnection);
  }

  public Proprietor getProprietor() {
    return proprietorRepository.findByUser(getLoggedInUser());
  }
}

