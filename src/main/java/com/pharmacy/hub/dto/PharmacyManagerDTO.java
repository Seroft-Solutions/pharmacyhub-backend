package com.pharmacy.hub.dto;

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
  private String area;
  private String city;
  private String experience;
  private String previousPharmacyName;
  private String currentJobStatus;
  private String shiftTime;
}

