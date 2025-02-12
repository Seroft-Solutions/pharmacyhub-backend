package com.pharmacy.hub.dto;

import com.pharmacy.hub.constants.ConnectionStatusEnum;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link PharmacistsConnections}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PharmacistsConnectionsDTO implements Serializable
{
    private Long id;
    private ConnectionStatusEnum connectionStatus;
    private String userGroup;
    private String notes;
    private String userId;
    private Long pharmacistId;
}