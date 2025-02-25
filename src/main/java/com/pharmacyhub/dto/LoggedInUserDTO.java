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
  private boolean registered;  // Changed from 'isRegistered' to 'registered' to match Java bean conventions
  private String jwtToken;
  private UserType userType;
  
  // Explicitly add getters and setters to ensure they're properly generated
  public boolean isOpenToConnect() {
    return openToConnect;
  }
  
  public void setOpenToConnect(boolean openToConnect) {
    this.openToConnect = openToConnect;
  }
  
  public boolean isRegistered() {
    return registered;
  }
  
  public void setRegistered(boolean registered) {
    this.registered = registered;
  }
  
  public String getJwtToken() {
    return jwtToken;
  }
  
  public void setJwtToken(String jwtToken) {
    this.jwtToken = jwtToken;
  }
  
  public UserType getUserType() {
    return userType;
  }
  
  public void setUserType(UserType userType) {
    this.userType = userType;
  }
}
