package com.pharmacy.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProprietorDTO implements PHUserDTO
{
  private Long id;
  private String licenseRequired;
  private String requiredLicenseDuration;
  private String pharmacyName;
  private String city;
  private String area;
  private String contactNumber;
}
