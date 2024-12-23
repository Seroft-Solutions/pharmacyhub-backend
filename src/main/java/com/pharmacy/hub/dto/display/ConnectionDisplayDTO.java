package com.pharmacy.hub.dto.display;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.hub.constants.StateEnum;
import com.pharmacy.hub.dto.PharmacistDTO;
import com.pharmacy.hub.dto.PharmacyManagerDTO;
import com.pharmacy.hub.dto.ProprietorDTO;
import com.pharmacy.hub.dto.SalesmanDTO;
import com.pharmacy.hub.dto.UserDTO;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.PharmacyManager;
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
