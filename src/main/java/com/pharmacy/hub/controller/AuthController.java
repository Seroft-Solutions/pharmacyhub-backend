package com.pharmacy.hub.controller;

import com.pharmacy.hub.dto.UserDTO;
import com.pharmacy.hub.keycloak.services.KeycloakUserService;
import com.pharmacy.hub.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class AuthController
{

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    private KeycloakUserService keycloakUserService;
    @Autowired
    @Lazy
    private UserService userService;


    @GetMapping("/token")
    public ResponseEntity<?> getToken(@AuthenticationPrincipal OidcUser oidcUser)
    {
        if (oidcUser != null)
        {

            String idToken = oidcUser.getIdToken().getTokenValue();
            String userId = oidcUser.getClaims().get("sub").toString();

            UserDTO userDTO = UserDTO.builder().id(userId)
                                     .emailAddress(oidcUser.getEmail())
                                     // .role(keycloakUserService.getUserRoles(oidcUser))
                                     .firstName(oidcUser.getClaims().get("given_name").toString())
                                     .lastName(oidcUser.getClaims().get("family_name").toString())
                    .registered(userService.isRegisteredUser(userId))
                                     .build();

            Map<String, Object> tokenResponse = new HashMap<>();
            tokenResponse.put("token", idToken);
            tokenResponse.put("user", userDTO);
            return ResponseEntity.ok(tokenResponse);
        }
        else
        {
            return ResponseEntity.status(401).body("User is not authenticated");
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken()
    {
        return ResponseEntity.ok().build();
    }

    //    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal Jwt jwt)
    {

        request.getSession().invalidate();

        String issuerUri = jwt.getIssuer().toString();
        String logoutUrl = issuerUri + "/protocol/openid-connect/logout";
        String idTokenHint = jwt.getTokenValue();

        String encodedRedirectUri = URLEncoder.encode(frontendUrl, StandardCharsets.UTF_8);
        String fullLogoutUrl = logoutUrl + "?id_token_hint=" + idTokenHint + "&post_logout_redirect_uri=" + encodedRedirectUri;

        // Return the logout URL and a flag indicating successful backend logout
        Map<String, Object> responseBackend = new HashMap<>();
        responseBackend.put("logoutUrl", fullLogoutUrl);
        responseBackend.put("backendLogoutSuccess", true);

        return ResponseEntity.ok().body(responseBackend);
    }

    @GetMapping("/debug/group-hierarchy")
    public ResponseEntity<?> debugGroupHierarchy()
    {
        return ResponseEntity.ok("Group hierarchy printed to console. Check your application logs.");
    }
}
