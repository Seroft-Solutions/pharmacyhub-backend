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
public class SalesmanDTO implements PHUserDTO
{
  private Long id;
  private String contactNumber;
  private String area;
  private String city;
  private String experience;
  private String previousPharmacyName;
  private String currentJobStatus;
  private String shiftTime;
  private boolean openToConnect;
  private boolean registered;
  private String firstName;
  private String lastName;
  private String emailAddress;
  
  @Override
  public UserType getUserType() {
    return UserType.SALESMAN;
  }
}
