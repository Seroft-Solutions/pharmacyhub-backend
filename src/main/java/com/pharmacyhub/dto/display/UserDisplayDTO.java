package com.pharmacyhub.dto.display;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.PharmacyManagerDTO;
import com.pharmacyhub.dto.ProprietorDTO;
import com.pharmacyhub.dto.SalesmanDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDisplayDTO
{
  private String firstName;
  private String lastName;
  private boolean connected;
  private PharmacistDTO pharmacist;
  private ProprietorDTO proprietor;
  private SalesmanDTO salesman;
  private PharmacyManagerDTO pharmacyManager;
}
