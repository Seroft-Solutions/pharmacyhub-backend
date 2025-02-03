package com.pharmacy.hub.keycloak.utils;

import com.pharmacy.hub.keycloak.services.KeycloakAuthService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KeycloakGroupUtils
{

    public static Optional<GroupRepresentation> findGroupByName(RealmResource realmResource, String groupName)
    {
        return realmResource.groups().groups().stream().filter(group -> group.getName().equals(groupName)).findFirst();
    }



    public static boolean isUserInGroup(RealmResource realmResource, String groupId, String userId)
    {
        return realmResource.groups().group(groupId).members().stream().anyMatch(member -> member.getId().equals(userId));
    }


    public static List<String> getGroupPath(GroupRepresentation group)
    {
        String[] pathParts = group.getPath().split("/");
        return Arrays.stream(pathParts).filter(part -> !part.isEmpty()).collect(Collectors.toList());
    }

    public static List<GroupRepresentation> getParentGroups(RealmResource realmResource, GroupRepresentation group)
    {
        List<GroupRepresentation> parents = new ArrayList<>();
        List<String> pathParts = getGroupPath(group);

        StringBuilder currentPath = new StringBuilder();
        for (String part : pathParts)
        {
            currentPath.append("/").append(part);
            findGroupByPath(realmResource, currentPath.toString()).ifPresent(parents::add);
        }
        return parents;
    }

    private static Optional<GroupRepresentation> findGroupByPath(RealmResource realmResource, String path)
    {
        return realmResource.groups().groups().stream().filter(g -> g.getPath().equals(path)).findFirst();
    }
}