package com.pharmacy.hub.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.hub.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements PHUserDTO
{
  private String id;
  private boolean openToConnect;
  private boolean registered;
  private String userType;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private String password;
  private String otpCode;
  @JsonIgnore
  private List<String> role;
  private PharmacistDTO pharmacist;
  private ProprietorDTO proprietor;
  private SalesmanDTO salesman;
  private PharmacyManagerDTO pharmacyManager;
}
