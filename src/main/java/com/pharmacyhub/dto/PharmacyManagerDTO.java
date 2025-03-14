package com.pharmacyhub.dto;

import com.pharmacyhub.entity.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PharmacyManagerDTO implements PHUserDTO
{
  private Long id;
  private String contactNumber;
  private String pharmacyName;
  private String city;
  private String location;
  private String experience;
  private String universityName;
  private String batch;
  private boolean openToConnect;
  private boolean registered;
  private String firstName;
  private String lastName;
  private String emailAddress;
  
  @Override
  public UserType getUserType() {
    return UserType.PHARMACY_MANAGER;
  }
}
