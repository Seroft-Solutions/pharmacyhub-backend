package com.pharmacyhub.service;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.constants.UserEnum;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.SalesmanDTO;
import com.pharmacyhub.dto.display.ConnectionDisplayDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.engine.PHMapper;
import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.connections.SalesmenConnections;
import com.pharmacyhub.repository.PharmacistRepository;
import com.pharmacyhub.repository.PharmacyManagerRepository;
import com.pharmacyhub.repository.ProprietorRepository;
import com.pharmacyhub.repository.SalesmanRepository;
import com.pharmacyhub.repository.connections.SalesmenConnectionsRepository;
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
  private PHMapper phMapper;

  @Override
  public PHUserDTO saveUser(PHUserDTO salesmanDTO)
  {
    Salesman salesman = phMapper.getSalesman((SalesmanDTO) salesmanDTO);
    getLoggedInUser().setRegistered(true);
    salesman.setUser(getLoggedInUser());
    getLoggedInUser().setUserType(UserEnum.SALESMAN.getUserEnum());
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

  @Override
  public PHUserDTO findUser(long id)
  {
    Optional<Salesman> salesman = salesmanRepository.findById(id);
    return phMapper.getSalesmanDTO(salesman.get());
  }

  @Override
  public List<UserDisplayDTO> findAllUsers()
  {
    return salesmanRepository.findAll().stream().map(salesman -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(salesman.getUser());
      userDisplayDTO.setSalesman(phMapper.getSalesmanDTO(salesman));

      userDisplayDTO.setConnected(getAllUserConnections().stream().anyMatch(userDisplayDTO1 -> {
        return userDisplayDTO1.getSalesman().getId().equals(salesman.getId());
      }));

      return userDisplayDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public void connectWith(PHUserConnectionDTO phUserConnectionDTO)
  {
    Salesman salesman = phMapper.getSalesman((SalesmanDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<SalesmenConnections> salesmanConnectionsList = salesmenConnectionsRepository.findByUserAndSalesmanAndState(getLoggedInUser(), salesman, StateEnum.READY_TO_CONNECT);

    if (salesmanConnectionsList.isEmpty())
    {
      SalesmenConnections salesmanConnections = new SalesmenConnections();
      salesmanConnections.setSalesman(salesman);
      salesmanConnections.setUser(getLoggedInUser());
      salesmenConnectionsRepository.save(salesmanConnections);
    }
  }

  @Override
  public List<UserDisplayDTO> getAllUserConnections()
  {
    List<SalesmenConnections> salesmanConnectionsList = salesmenConnectionsRepository.findByUserAndState(getLoggedInUser(), StateEnum.READY_TO_CONNECT);

    return salesmanConnectionsList.stream().map(salesmanConnection -> {
      UserDisplayDTO userDisplayDTO = phMapper.getUserDisplayDTO(salesmanConnection.getSalesman().getUser());
      userDisplayDTO.setSalesman(phMapper.getSalesmanDTO(salesmanConnection.getSalesman()));
      return userDisplayDTO;
    }).collect(Collectors.toList());

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
    salesmenConnections.setState(userConnectionDTO.getState());
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
    Salesman salesman = phMapper.getSalesman((SalesmanDTO) findUser(phUserConnectionDTO.getConnectWith()));
    List<SalesmenConnections> salesmanConnectionsList = salesmenConnectionsRepository.findByUserAndSalesmanAndState(getLoggedInUser(), salesman, StateEnum.READY_TO_CONNECT);

    SalesmenConnections salesmanConnection = salesmanConnectionsList.stream().findFirst().get();
    salesmanConnection.setState(StateEnum.CLIENT_DISCONNECT);
    salesmenConnectionsRepository.save(salesmanConnection);
  }
  
  public Salesman getSalesman(){
    return salesmanRepository.findByUser(getLoggedInUser());
  }
}



