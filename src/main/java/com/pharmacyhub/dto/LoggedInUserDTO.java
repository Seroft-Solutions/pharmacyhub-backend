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
public class LoggedInUserDTO
{
  private boolean openToConnect;
  private boolean isRegistered;
  private String jwtToken;
  private UserType userType;
}
