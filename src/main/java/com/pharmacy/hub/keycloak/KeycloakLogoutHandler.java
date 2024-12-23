package com.pharmacy.hub.keycloak;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KeycloakLogoutHandler implements LogoutHandler
{

    private final RestTemplate restTemplate;


    public KeycloakLogoutHandler()
    {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    {
        if (authentication != null) {
            logoutFromKeycloak((OidcUser) authentication.getPrincipal());
        }
    }

    private void logoutFromKeycloak(OidcUser user)
    {
        String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endSessionEndpoint).queryParam("id_token_hint", user.getIdToken().getTokenValue());

        ResponseEntity<String> logoutResponse = restTemplate.getForEntity(builder.toUriString(), String.class);
        if (logoutResponse.getStatusCode().is2xxSuccessful())
        {
            System.out.println("Successfully logged out from Keycloak");
        }
        else
        {
            System.out.println("Could not propagate logout to Keycloak");
        }
    }
}