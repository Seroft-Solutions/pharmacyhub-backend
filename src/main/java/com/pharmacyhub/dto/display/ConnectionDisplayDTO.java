package com.pharmacyhub.dto.display;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.Proprietor;
import com.pharmacyhub.entity.Salesman;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionDisplayDTO
{
  private Long id;
  private StateEnum state;
  private String notes;
  private Date createdAt;
  private Date updatedAt;
  private UserDTO user;
  
  private Pharmacist pharmacist;
  private Proprietor proprietor;
  private Salesman salesman;
  private PharmacyManager pharmacyManager;
}
