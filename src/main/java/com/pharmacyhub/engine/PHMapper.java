package com.pharmacyhub.engine;

import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.PharmacyManagerDTO;
import com.pharmacyhub.dto.ProprietorDTO;
import com.pharmacyhub.dto.ReportingUserDTO;
import com.pharmacyhub.dto.SalesmanDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.dto.display.ConnectionDisplayDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.Proprietor;
import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.connections.PharmacistsConnections;
import com.pharmacyhub.entity.connections.PharmacyManagerConnections;
import com.pharmacyhub.entity.connections.ProprietorsConnections;
import com.pharmacyhub.entity.connections.SalesmenConnections;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PHMapper
{
  private ModelMapper modelMapper = new ModelMapper();

  public Proprietor getProprietor(ProprietorDTO proprietorDTO)
  {
    return modelMapper.map(proprietorDTO, Proprietor.class);
  }

  public ProprietorDTO getProprietorDTO(Proprietor proprietor)
  {
    return modelMapper.map(proprietor, ProprietorDTO.class);
  }

  public Pharmacist getPharmacist(PharmacistDTO pharmacistDTO)
  {
    return modelMapper.map(pharmacistDTO, Pharmacist.class);
  }

  public PharmacistDTO getPharmacistDTO(Pharmacist pharmacist)
  {
    return modelMapper.map(pharmacist, PharmacistDTO.class);
  }

  public UserDTO getUserDTO(User user)
  {
    return modelMapper.map(user, UserDTO.class);
  }

  public User getUser(UserDTO userDTO)
  {
    return modelMapper.map(userDTO, User.class);
  }

  public PharmacyManager getPharmacyManager(PharmacyManagerDTO pharmacyManagerDTO)
  {
    return modelMapper.map(pharmacyManagerDTO, PharmacyManager.class);
  }

  public PharmacyManagerDTO getPharmacyManagerDTO(PharmacyManager pharmacyManager)
  {
    return modelMapper.map(pharmacyManager, PharmacyManagerDTO.class);
  }

  public Salesman getSalesman(SalesmanDTO salesmanDTO)
  {
    return modelMapper.map(salesmanDTO, Salesman.class);
  }

  public SalesmanDTO getSalesmanDTO(Salesman salesman)
  {
    return modelMapper.map(salesman, SalesmanDTO.class);
  }

  public UserDisplayDTO getUserDisplayDTO(User user)
  {
    return modelMapper.map(user, UserDisplayDTO.class);
  }

  public ReportingUserDTO getReportingUserDTO(User user)
  {
    return modelMapper.map(user, ReportingUserDTO.class);
  }

  public ConnectionDisplayDTO getConnectionDisplayDTO(ProprietorsConnections pharmacistsConnections)
  {
    return modelMapper.map(pharmacistsConnections, ConnectionDisplayDTO.class);
  }

  public ConnectionDisplayDTO getConnectionDisplayDTO(SalesmenConnections pharmacistsConnections)
  {
    return modelMapper.map(pharmacistsConnections, ConnectionDisplayDTO.class);
  }

  public ConnectionDisplayDTO getConnectionDisplayDTO(PharmacistsConnections pharmacistsConnections)
  {
    return modelMapper.map(pharmacistsConnections, ConnectionDisplayDTO.class);
  }

  public ConnectionDisplayDTO getConnectionDisplayDTO(PharmacyManagerConnections pharmacistsConnections)
  {
    return modelMapper.map(pharmacistsConnections, ConnectionDisplayDTO.class);
  }

}
