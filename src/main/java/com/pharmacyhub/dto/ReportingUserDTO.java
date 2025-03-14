package com.pharmacyhub.dto;

import com.pharmacyhub.entity.enums.UserType;

public class ReportingUserDTO extends UserDTO
{
  private String contactNumber;
  
  @Override
  public UserType getUserType() {
    return super.getUserType();
  }
}
