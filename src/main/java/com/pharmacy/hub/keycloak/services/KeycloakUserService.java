package com.pharmacy.hub.keycloak.services;

import com.pharmacy.hub.dto.UserDTO;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;

//@CacheConfig(cacheNames = "keycloak-users")
public interface KeycloakUserService
{

   // @CacheEvict(allEntries = true)
    String createUser(UserDTO userDTO);

    //@CacheEvict(allEntries = true)
    String createBusinessPartner(UserDTO userDTO);

    //@CacheEvict(allEntries = true)
    void updateUser(String userId, UserDTO userDTO);

    //@CacheEvict(allEntries = true)
    void deleteUser(String userId);

    //@Cacheable(key = "'user-info-' + #userId")
    UserDTO getUserInfo(String userId);

    //@CacheEvict(allEntries = true)
    void connectWithBusinessPartner(String userId);

    //@CacheEvict(allEntries = true)
    void disconnectWithBusinessPartner(String userId);

    //@Cacheable(key = "'business-partner-users-' + #identifier")
    List<UserDTO> getAllUsersBusinessPartner(String identifier);

    //@Cacheable(key = "'pageable-business-partner-users-' + #name + '-' + #pageable.pageNumber + '-' + #pageable" +
    //        ".pageSize")
    Page<UserDTO> getAllPageableUsersBusinessPartner_V2(Pageable pageable, String name);

    //@Cacheable(key = "'all-users'")
    List<UserRepresentation> getUsers();

    //@CacheEvict(allEntries = true)
    UserDTO updateBusinessPicture(String userId, String pictureBase64);

    //@Cacheable(key = "'subgroup-users'")
    List<UserDTO> getAllUsersInSubgroup();

    //@Cacheable(key = "'pageable-subgroup-users-' + #name + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    Page<UserDTO> getAllPagableUsersInSubgroup(Pageable pageable, String name);

    //@Cacheable(key = "'paginated-users-' + #name + '-' + #pageNumber + '-' + #pageSize")
    Page<UserDTO> getPaginatedUsers(int pageNumber, int pageSize, String name);

    List<String> getUserRoles(OidcUser oidcUser);
}
