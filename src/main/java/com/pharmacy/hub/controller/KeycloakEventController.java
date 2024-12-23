package com.pharmacy.hub.controller;

import com.pharmacy.hub.dto.KeycloakEventDTO;
import com.pharmacy.hub.keycloak.KeycloakEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keycloak-events")
public class KeycloakEventController
{

    @Autowired
    private KeycloakEventService keycloakEventService;

    @PostMapping
    public ResponseEntity<String> handleKeycloakEvent(@RequestBody KeycloakEventDTO eventDTO)
    {
        keycloakEventService.processRegistrationEvent(eventDTO);
        return ResponseEntity.ok("Event processed successfully");
    }
}