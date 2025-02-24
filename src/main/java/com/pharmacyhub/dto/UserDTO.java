package com.pharmacyhub.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacyhub.entity.SystemRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements PHUserDTO
{
  private Long id;
  private boolean openToConnect;
  private boolean registered;
  private String userType;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private String password;
  private String otpCode;
   @JsonIgnore
  private SystemRole role;
  private PharmacistDTO pharmacist;
  private ProprietorDTO proprietor;
  private SalesmanDTO salesman;
  private PharmacyManagerDTO pharmacyManager;
}
