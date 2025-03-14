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
public class PharmacistDTO implements PHUserDTO
{
  private Long id;
  private String categoryAvailable;
  private String licenseDuration;
  private String experience;
  private String city;
  private String location;
  private String universityName;
  private String batch;
  private String contactNumber;
  private String categoryProvince;
  private boolean openToConnect;
  private boolean registered;
  private String firstName;
  private String lastName;
  private String emailAddress;
  
  @Override
  public UserType getUserType() {
    return UserType.PHARMACIST;
  }
}
