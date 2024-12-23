package com.pharmacy.hub.keycloak;

import com.pharmacy.hub.dto.KeycloakEventDTO;
import org.springframework.stereotype.Service;

@Service
public class KeycloakEventService
{

    public void processRegistrationEvent(KeycloakEventDTO eventDTO)
    {
        // Here you can implement your custom logic for handling the registration event
        // For example:
        // 1. Send a welcome email
        // 2. Create additional user profile information
        // 3. Sync user data with other systems

        System.out.println("Processing registration event for user: " + eventDTO.getUsername());
        System.out.println("User details: " + eventDTO);

        // Add your custom logic here
    }
}