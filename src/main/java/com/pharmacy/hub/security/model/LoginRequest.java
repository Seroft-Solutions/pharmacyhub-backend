package com.pharmacy.hub.security.model;

import lombok.Data;

@Data
public class LoginRequest
{
  private String emailAddress;
  private String password;
}
