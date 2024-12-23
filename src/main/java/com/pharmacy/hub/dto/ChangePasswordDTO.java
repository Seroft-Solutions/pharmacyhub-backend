package com.pharmacy.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO
{
  private Long id;
  private String emailAddress;
  private String currentPassword;
  private String newPassword;
}
