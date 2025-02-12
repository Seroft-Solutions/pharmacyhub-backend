package com.pharmacy.hub.keycloak.utils;

import com.pharmacy.hub.security.TenantContext;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class KeycloakUtils {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakUtils.class);
    private static final int MAX_RESULTS = 10000000;

    public static String getCurrentUserTenantGroup() {
        return TenantContext.getCurrentTenant();
    }

    public static String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof OidcUser oidcUser) {
                return oidcUser.getPreferredUsername();
            } else if (principal instanceof Jwt jwt) {
                return (String) jwt.getClaims().get("preferred_username");
            }
        }

        return null;
    }


    public static int getMaxResults() {
        return MAX_RESULTS;
    }

    public Optional<GroupRepresentation> findGroupByName(RealmResource realmResource, String groupName) {
        List<GroupRepresentation> groups = realmResource.groups().groups();
        for (GroupRepresentation group : groups) {
            if (group.getName().equals(groupName)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }

    public GroupRepresentation findSubGroupByName(RealmResource realmResource, String parentGroupId,
                                                  String subGroupName) {
        List<GroupRepresentation> subGroups =
                realmResource.groups().group(parentGroupId).getSubGroups(0, MAX_RESULTS, false);
        for (GroupRepresentation subGroup : subGroups) {
            if (subGroup.getName().equals(subGroupName)) {
                return subGroup;
            }
        }
        return null;
    }

    public List<GroupRepresentation> getSubGroups(RealmResource realmResource, String groupId) {
        return realmResource.groups().group(groupId).getSubGroups(0, MAX_RESULTS, false);
    }
}