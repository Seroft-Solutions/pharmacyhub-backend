package com.pharmacy.hub.dto;

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
}
