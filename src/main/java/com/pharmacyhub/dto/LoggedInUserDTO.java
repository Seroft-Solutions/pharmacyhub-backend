package com.pharmacyhub.dto;

import com.pharmacyhub.entity.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoggedInUserDTO {
  private Long id;
  private String emailAddress;
  private String firstName;
  private String lastName;
  private boolean openToConnect;
  private boolean registered;
  private String jwtToken;
  private UserType userType;
  private List<String> roles;
  private List<String> permissions;
  
  // Explicit getters and setters for proper serialization
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getEmailAddress() {
    return emailAddress;
  }
  
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
  
  public String getFirstName() {
    return firstName;
  }
  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
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
  
  public List<String> getRoles() {
    return roles;
  }
  
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
  
  public List<String> getPermissions() {
    return permissions;
  }
  
  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }
}
