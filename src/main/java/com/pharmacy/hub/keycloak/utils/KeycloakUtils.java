package com.pharmacy.hub.keycloak.utils;


import com.pharmacy.hub.security.TenantContext;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakUtils
{
    private static final Logger logger = LoggerFactory.getLogger(KeycloakUtils.class);
    private static final int MAX_RESULTS = 10000000;

    public static String getCurrentUserBusinessGroup()
    {
        return TenantContext.getCurrentTenant();
    }

    public static int getMaxResults()
    {
        return MAX_RESULTS;
    }

    public GroupRepresentation findGroupByName(RealmResource realmResource, String groupName)
    {
        List<GroupRepresentation> groups = realmResource.groups().groups();
        return groups.stream().filter(group -> group.getName().equals(groupName)).findFirst().orElse(null);
    }

    public GroupRepresentation findSubGroupByName(RealmResource realmResource, String parentGroupId,
                                                  String subGroupName)
    {
        List<GroupRepresentation> subGroups =
                realmResource.groups().group(parentGroupId).getSubGroups(0, MAX_RESULTS, false);
        return subGroups.stream().filter(subGroup -> subGroup.getName().equals(subGroupName)).findFirst().orElse(null);
    }

    public List<GroupRepresentation> getSubGroups(RealmResource realmResource, String groupId)
    {
        return realmResource.groups().group(groupId).getSubGroups(0, MAX_RESULTS, false);
    }
}