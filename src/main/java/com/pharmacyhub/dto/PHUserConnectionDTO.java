package com.pharmacyhub.dto;

import com.pharmacyhub.constants.StateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PHUserConnectionDTO
{
  private long id;
  private long connectWith;
  private StateEnum state;
  private String notes;
}
