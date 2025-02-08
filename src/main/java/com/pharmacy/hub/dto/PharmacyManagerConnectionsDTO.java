package com.pharmacy.hub.dto;

import com.pharmacy.hub.constants.ConnectionStatusEnum;
import com.pharmacy.hub.entity.connections.PharmacistsConnections;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link PharmacistsConnections}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PharmacyManagerConnectionsDTO implements Serializable
{
    private Long id;
    private ConnectionStatusEnum connectionStatus;
    private String userGroup;
    private String notes;
    private String userId;
    private Long pharmacyManagerId;
}