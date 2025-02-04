package com.pharmacy.hub.keycloak.services.Implementation;

import com.pharmacy.hub.dto.UserDTO;
import com.pharmacy.hub.keycloak.services.KeycloakAuthService;
import com.pharmacy.hub.keycloak.services.KeycloakGroupService;
import com.pharmacy.hub.keycloak.services.KeycloakUserService;
import com.pharmacy.hub.keycloak.utils.KeycloakUtils;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class KeycloakUserServiceImpl implements KeycloakUserService
{
    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserServiceImpl.class);

    private final KeycloakAuthService keycloakAuthService;
    private final KeycloakGroupService keycloakGroupService;
//    private final KeycloakRoleService keycloakRoleService;
//    private final UserProfileService userProfileService;
    private final KeycloakUtils keycloakUtils;
    private final String realm;

    public KeycloakUserServiceImpl(
            KeycloakAuthService keycloakAuthService,
            KeycloakGroupService keycloakGroupService,
//            KeycloakRoleService keycloakRoleService,
//            UserProfileService userProfileService,
            KeycloakUtils keycloakUtils,
            @org.springframework.beans.factory.annotation.Value("${keycloak.realm}") String realm)
    {
        this.keycloakAuthService = keycloakAuthService;
        this.keycloakGroupService = keycloakGroupService;
//        this.keycloakRoleService = keycloakRoleService;
//        this.userProfileService = userProfileService;
        this.keycloakUtils = keycloakUtils;
        this.realm = realm;
    }

    @Override
    @Transactional
    public String createUser(UserDTO userDTO)
    {
        logger.info("Creating user. Username: {}, Email: {}", userDTO.getEmailAddress(), userDTO.getEmailAddress());
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation user = createUserRepresentation(userDTO);
        Response response = usersResource.create(user);
        String userId = extractUserIdFromResponse(response);

        if (response.getStatus() == 201)
        {
            logger.info("User created successfully. UserId: {}", userId);
            //setInitialPassword(usersResource.get(userId));
            //assignGroupsAndRoles(userId, userDTO);
            return userId;
        }

        logger.error("Failed to create user");
        throw new RuntimeException("Failed to create user");
    }

    @Override
    public String createBusinessPartner(UserDTO userDTO)
    {
        return "";
    }

    @Override
    public void updateUser(String userId, UserDTO userDTO)
    {

    }

    @Override
    public void deleteUser(String userId)
    {

    }
    public UserRepresentation getUserById(String userId) {
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        return realmResource.users().get(userId).toRepresentation();
    }

    @Override
    public UserDTO getUserInfo(String userId)
    {
        return null;
    }

    @Override
    public void connectWithBusinessPartner(String userId)
    {

    }

    @Override
    public void disconnectWithBusinessPartner(String userId)
    {

    }

    @Override
    public List<UserDTO> getAllUsersBusinessPartner(String identifier)
    {
        return List.of();
    }

    @Override
    public Page<UserDTO> getAllPageableUsersBusinessPartner_V2(Pageable pageable, String name)
    {
        return null;
    }

    @Override
    public List<UserRepresentation> getUsers()
    {
        return List.of();
    }

    @Override
    public UserDTO updateBusinessPicture(String userId, String pictureBase64)
    {
        return null;
    }

    @Override
    public List<UserDTO> getAllUsersInSubgroup()
    {
        return List.of();
    }

    @Override
    public Page<UserDTO> getAllPagableUsersInSubgroup(Pageable pageable, String name)
    {
        return null;
    }

    @Override
    public Page<UserDTO> getPaginatedUsers(int pageNumber, int pageSize, String name)
    {
        return null;
    }

    private UserRepresentation createUserRepresentation(UserDTO userDTO)
    {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getEmailAddress());
        user.setEmail(userDTO.getEmailAddress());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEnabled(true);
        return user;
    }

    private String extractUserIdFromResponse(Response response)
    {
        return response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
    }




    public List<String> getUserRoles(OidcUser oidcUser)
    {
        List<String> roles = new ArrayList<>();
        if (oidcUser != null)
        {
            Map<String, Object> realmAccess = oidcUser.getClaim("realm_access");
            if (realmAccess != null)
            {
                List<String> extractedRoles = (List<String>) realmAccess.get("roles");
                if (extractedRoles != null)
                {
                    roles.addAll(extractedRoles);
                }
                else
                {
                    System.out.println("No roles found in realm_access");
                }
            }
            else
            {
                System.out.println("realm_access claim is null");
            }
        }
        else
        {
            System.out.println("OidcUser not found");
        }
        return roles;
    }

    public List<String> getUserGroups(OidcUser oidcUser)
    {
        if (oidcUser != null)
        {
            return oidcUser.getClaimAsStringList("groups");
        }
        else
        {
            System.out.println("OidcUser not found");
            return new ArrayList<>();
        }
    }
}