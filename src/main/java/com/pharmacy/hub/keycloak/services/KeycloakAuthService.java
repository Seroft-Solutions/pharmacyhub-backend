package com.pharmacy.hub.keycloak.services;

import org.keycloak.admin.client.Keycloak;

public interface KeycloakAuthService
{
    Keycloak getKeycloakInstance();
}
