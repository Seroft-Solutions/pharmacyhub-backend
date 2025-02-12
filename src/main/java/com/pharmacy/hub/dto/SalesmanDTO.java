package com.pharmacy.hub.dto;

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
  private String experience;
  private String education;
  private String contactNumber;
  private String area;
  private String city;
  private String previousPharmacyName;
  private String currentJobStatus;
  private String timePrefernce;
  private String saleryExpectation;
}
