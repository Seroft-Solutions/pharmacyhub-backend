package com.pharmacy.hub.engine;

import com.pharmacy.hub.dto.*;
import com.pharmacy.hub.dto.display.ConnectionDisplayDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.PharmacyManager;
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import com.pharmacy.hub.entity.connections.PharmacyManagerConnections;
import com.pharmacy.hub.entity.connections.ProprietorsConnections;
import com.pharmacy.hub.entity.connections.SalesmenConnections;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PHMapper
{
    private final ModelMapper modelMapper = new ModelMapper();

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

    public PharmacistsConnections getPharmacistsConnections(PharmacistsConnectionsDTO pharmacistsConnectionsDTO)
    {
        return modelMapper.map(pharmacistsConnectionsDTO, PharmacistsConnections.class);
    }
    public PharmacyManagerConnections getPharmacyManagerConnections(PharmacyManagerConnectionsDTO pharmacyManagerConnectionsDTO)
    {
        return modelMapper.map(pharmacyManagerConnectionsDTO, PharmacyManagerConnections.class);
    }
    public ProprietorsConnections getProprietorConnections(ProprietorsConnectionsDTO proprietorConnectionsDTO)
    {
        return modelMapper.map(proprietorConnectionsDTO, ProprietorsConnections.class);
    }

    public SalesmenConnections getSalesmenConnections(SalesmenConnectionsDTO salesmanConnectionsDTO)
    {
        return modelMapper.map(salesmanConnectionsDTO, SalesmenConnections.class);
    }
}
