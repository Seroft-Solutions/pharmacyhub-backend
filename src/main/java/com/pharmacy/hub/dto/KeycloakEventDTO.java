package com.pharmacy.hub.dto;

import lombok.Data;



@Data
public class KeycloakEventDTO
{
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}