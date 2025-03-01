package com.pharmacyhub.dto;

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
}
