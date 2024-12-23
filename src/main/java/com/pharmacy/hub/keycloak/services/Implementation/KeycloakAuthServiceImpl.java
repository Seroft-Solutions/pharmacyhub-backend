package com.pharmacy.hub.keycloak.services.Implementation;

import com.pharmacy.hub.keycloak.services.KeycloakAuthService;
import com.pharmacy.hub.keycloak.services.KeycloakAuthService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakAuthServiceImpl implements KeycloakAuthService
{

    private static KeycloakAuthServiceImpl instance;
    private Keycloak keycloakInstance;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${keycloak.realm}")
    private String realm;

    private KeycloakAuthServiceImpl()
    {
    }

    public static synchronized KeycloakAuthServiceImpl getInstance()
    {
        if (instance == null)
        {
            instance = new KeycloakAuthServiceImpl();
        }
        return instance;
    }

    @Override
    public synchronized Keycloak getKeycloakInstance()
    {
        if (keycloakInstance == null)
        {
            keycloakInstance = KeycloakBuilder.builder()
                                              .serverUrl(issuerUri.replace("/realms/" + realm, ""))
                                              .realm("master")
                                              .clientId("admin-cli")
                                              .username(adminUsername)
                                              .password(adminPassword)
                                              .build();
        }
        return keycloakInstance;
    }
}