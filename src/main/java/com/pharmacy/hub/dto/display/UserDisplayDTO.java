package com.pharmacy.hub.dto.display;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.hub.dto.PharmacistDTO;
import com.pharmacy.hub.dto.PharmacyManagerDTO;
import com.pharmacy.hub.dto.ProprietorDTO;
import com.pharmacy.hub.dto.SalesmanDTO;
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
