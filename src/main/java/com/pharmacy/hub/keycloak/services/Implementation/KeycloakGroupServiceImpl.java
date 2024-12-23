package com.pharmacy.hub.keycloak.services.Implementation;

import com.pharmacy.hub.keycloak.services.KeycloakAuthService;
import com.pharmacy.hub.keycloak.services.KeycloakGroupService;
import com.pharmacy.hub.keycloak.utils.KeycloakUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakGroupServiceImpl implements KeycloakGroupService
{
    private static final Logger logger = LoggerFactory.getLogger(KeycloakGroupServiceImpl.class);

    private final KeycloakAuthService keycloakAuthService;
    private final KeycloakUtils keycloakUtils;
    private final String realm;

    public KeycloakGroupServiceImpl(KeycloakAuthService keycloakAuthService,
                                    KeycloakUtils keycloakUtils,
                                    @Value("${keycloak.realm}") String realm)
    {
        this.keycloakAuthService = keycloakAuthService;
        this.keycloakUtils = keycloakUtils;
        this.realm = realm;
    }

    @Override
    public boolean checkAndCreateUserChildGroup(String userId)
    {
        logger.info("Checking and creating child group for user: {}", userId);
        boolean isInChildGroup = false;
        try
        {
            Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            String username = user.getUsername();

            List<GroupRepresentation> topLevelGroups = realmResource.groups().groups();

            for (GroupRepresentation topLevelGroup : topLevelGroups)
            {
                if (topLevelGroup.getName().equals("BUSINESS_PARTNER") ||
                        topLevelGroup.getName().equals("SUPER_ADMIN"))
                {
                    continue;
                }

                if (isUserInChildGroup(realmResource, topLevelGroup, userId, username))
                {
                    isInChildGroup = true;
                    logger.info("User {} is already in a child group", userId);
                    break;
                }
            }

            if (!isInChildGroup && userResource.groups().isEmpty())
            {
                logger.info("User {} is not in a child group. Creating child groups...", userId);
                createChildGroupsForUser(realmResource, userId, username);
                isInChildGroup = true;
            }
        }
        catch (Exception e)
        {
            logger.error("Error checking and creating child group for user: {}", userId, e);
        }
        return isInChildGroup;
    }
//TODO: Need some modification
    private boolean isUserInChildGroup(RealmResource realmResource, GroupRepresentation group,
                                       String userId, String username)
    {
        List<GroupRepresentation> subGroups = realmResource.groups()
                                                           .group(group.getId())
                                                           .getSubGroups(0, KeycloakUtils.getMaxResults(), false);

        for (GroupRepresentation subGroup : subGroups)
        {
            if (subGroup.getName().equals(username))
            {
                List<UserRepresentation> groupMembers = realmResource.groups()
                                                                     .group(subGroup.getId())
                                                                     .members();
                if (groupMembers.stream().anyMatch(member -> member.getId().equals(userId)))
                {
                    return true;
                }
            }
            if (isUserInChildGroup(realmResource, subGroup, userId, username))
            {
                return true;
            }
        }
        return false;
    }

    private void createChildGroupsForUser(RealmResource realmResource, String userId, String username)
    {
        List<GroupRepresentation> topLevelGroups = realmResource.groups().groups();
        for (GroupRepresentation topLevelGroup : topLevelGroups)
        {
            if (topLevelGroup.getName().equals("BUSINESS_PARTNER") ||
                    topLevelGroup.getName().equals("SUPER_ADMIN"))
            {
                continue;
            }
            createChildGroupIfNotExists(topLevelGroup.getId(), username);
            assignUserToChildGroup(userId, topLevelGroup.getId(), username);
        }
    }

    @Override
    public void createChildGroupAndAssignUser(String userId, String email)
    {
        String domain = email.substring(email.indexOf("@") + 1);
        List<GroupRepresentation> allGroups = keycloakAuthService.getKeycloakInstance()
                                                                 .realm(realm)
                                                                 .groups()
                                                                 .groups();

        for (GroupRepresentation group : allGroups)
        {
            createChildGroupIfNotExists(group.getId(), domain);
            assignUserToChildGroup(userId, group.getId(), domain);
        }
    }

    @Override
    public void createChildGroupIfNotExists(String parentGroupId, String childGroupName)
    {
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        GroupResource parentGroup = realmResource.groups().group(parentGroupId);

        List<GroupRepresentation> subGroups = parentGroup.getSubGroups(0, KeycloakUtils.getMaxResults(), false);
        boolean childGroupExists = subGroups.stream()
                                            .anyMatch(group -> group.getName().equals(childGroupName));

        if (!childGroupExists)
        {
            GroupRepresentation newChildGroup = new GroupRepresentation();
            newChildGroup.setName(childGroupName);
            parentGroup.subGroup(newChildGroup);
            logger.info("Created child group '{}' under parent group ID: {}", childGroupName, parentGroupId);
        }
    }

    @Override
    public void assignUserToChildGroup(String userId, String parentGroupId, String childGroupName)
    {
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        GroupResource parentGroup = realmResource.groups().group(parentGroupId);

        List<GroupRepresentation> subGroups = parentGroup.getSubGroups(0, KeycloakUtils.getMaxResults(), false);
        subGroups.stream()
                 .filter(group -> group.getName().equals(childGroupName))
                 .findFirst()
                 .ifPresent(childGroup -> {
                     realmResource.users().get(userId).joinGroup(childGroup.getId());
                     logger.info("Assigned user {} to child group '{}' under parent group ID: {}",
                                 userId, childGroupName, parentGroupId);
                 });
    }

    @Override
    public void assignUserToBusinessGroup(String userId, String parentGroupId, String businessGroupName)
    {
        logger.info("Assigning user {} to business group {} under parent group {}",
                    userId, businessGroupName, parentGroupId);

        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        try
        {
            GroupResource parentGroupResource = realmResource.groups().group(parentGroupId);
            GroupRepresentation businessGroup = parentGroupResource
                    .getSubGroups(0, KeycloakUtils.getMaxResults(), false)
                    .stream()
                    .filter(group -> group.getName().equals(businessGroupName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Business group not found: " + businessGroupName));

            userResource.joinGroup(businessGroup.getId());
            logger.info("Successfully assigned user {} to business group {} (ID: {})",
                        userId, businessGroupName, businessGroup.getId());
        }
        catch (Exception e)
        {
            logger.error("Failed to assign user {} to business group {} under parent group {}",
                         userId, businessGroupName, parentGroupId, e);
            throw new RuntimeException("Failed to assign user to business group", e);
        }
    }

    @Override
    public void assignUserToGroup(String userId, String groupId)
    {
        logger.info("Assigning user {} to group {}", userId, groupId);
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UserResource userResource = realmResource.users().get(userId);
        userResource.joinGroup(groupId);
    }

    @Override
    public void unassignUserToGroup(String userId, String groupId)
    {
        logger.info("Unassigning user {} from group {}", userId, groupId);
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UserResource userResource = realmResource.users().get(userId);
        userResource.leaveGroup(groupId);
    }

    @Override
    public void moveUserToAnotherSubGroup(String userId, String sourceGroupName,
                                          String sourceSubGroupName, String targetGroupName,
                                          String targetSubGroupName)
    {
        removeUserFromSubGroup(userId, sourceGroupName, sourceSubGroupName);
        addUserToSubGroup(userId, targetGroupName, targetSubGroupName);
        logger.info("Moved user {} from {}/{} to {}/{}",
                    userId, sourceGroupName, sourceSubGroupName, targetGroupName, targetSubGroupName);
    }

    @Override
    public void addUserToSubGroup(String userId, String groupName, String subGroupName)
    {
        logger.info("Adding user {} to subgroup: {} under group: {}", userId, subGroupName, groupName);
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        GroupRepresentation group = keycloakUtils.findGroupByName(realmResource, groupName);
        if (group != null)
        {
            GroupRepresentation subGroup = keycloakUtils.findSubGroupByName(realmResource, group.getId(), subGroupName);
            if (subGroup != null)
            {
                userResource.joinGroup(subGroup.getId());
                logger.info("Added user to subgroup: {} (ID: {})", subGroup.getName(), subGroup.getId());
            }
            else
            {
                logger.error("Subgroup not found: {} under group: {}", subGroupName, groupName);
                throw new RuntimeException("Subgroup not found: " + subGroupName + " under group: " + groupName);
            }
        }
        else
        {
            logger.error("Group not found: {}", groupName);
            throw new RuntimeException("Group not found: " + groupName);
        }
    }

    @Override
    public void removeUserFromSubGroup(String userId, String groupName, String subGroupName)
    {
        logger.info("Removing user {} from subgroup: {} under group: {}", userId, subGroupName, groupName);
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        GroupRepresentation group = keycloakUtils.findGroupByName(realmResource, groupName);
        if (group != null)
        {
            GroupRepresentation subGroup = keycloakUtils.findSubGroupByName(realmResource, group.getId(), subGroupName);
            if (subGroup != null)
            {
                userResource.leaveGroup(subGroup.getId());
                logger.info("Removed user from subgroup: {} (ID: {})", subGroup.getName(), subGroup.getId());
            }
            else
            {
                logger.error("Subgroup not found: {} under group: {}", subGroupName, groupName);
                throw new RuntimeException("Subgroup not found: " + subGroupName + " under group: " + groupName);
            }
        }
        else
        {
            logger.error("Group not found: {}", groupName);
            throw new RuntimeException("Group not found: " + groupName);
        }
    }

    @Override
    public List<GroupRepresentation> getGroupsContainingSubgroup(String subgroupName)
    {
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        List<GroupRepresentation> allGroups = realmResource.groups().groups();
        return allGroups.stream()
                        .filter(group -> hasSubgroup(realmResource, group, subgroupName))
                        .toList();
    }

    private boolean hasSubgroup(RealmResource realmResource, GroupRepresentation group, String subgroupName)
    {
        List<GroupRepresentation> subGroups = realmResource.groups()
                                                           .group(group.getId())
                                                           .getSubGroups(0, KeycloakUtils.getMaxResults(), false);
        return subGroups.stream().anyMatch(subGroup -> subGroup.getName().equals(subgroupName)) ||
                subGroups.stream().anyMatch(subGroup -> hasSubgroup(realmResource, subGroup, subgroupName));
    }

    @Override
    public List<UserRepresentation> getUsersInSubGroup(String groupName, String subGroupName)
    {
        logger.info("Retrieving users in subgroup: {} under group: {}", subGroupName, groupName);
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);

        GroupRepresentation group = keycloakUtils.findGroupByName(realmResource, groupName);
        if (group != null)
        {
            GroupRepresentation subGroup = keycloakUtils.findSubGroupByName(realmResource, group.getId(), subGroupName);
            if (subGroup != null)
            {
                return realmResource.groups().group(subGroup.getId()).members();
            }
            else
            {
                logger.error("Subgroup not found: {} under group: {}", subGroupName, groupName);
                throw new RuntimeException("Subgroup not found: " + subGroupName + " under group: " + groupName);
            }
        }
        else
        {
            logger.error("Group not found: {}", groupName);
            throw new RuntimeException("Group not found: " + groupName);
        }
    }

    @Override
    public void printGroupHierarchy()
    {
        logger.info("Printing group hierarchy");
        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        List<GroupRepresentation> topLevelGroups = realmResource.groups().groups();
        for (GroupRepresentation group : topLevelGroups)
        {
            printGroup(group, 0);
        }
    }

    @Override
    public String getUserTenantGroup(String email)
    {
        logger.info("Retrieving tenant group for user: {}", email);

        Keycloak keycloak = keycloakAuthService.getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UserRepresentation user = realmResource.users().search(email).stream().findFirst().orElse(null);

        if (user == null)
        {
            return null;
        }

        UserResource userResource = realmResource.users().get(user.getId());
        List<GroupRepresentation> groups = userResource.groups();

        if (!groups.isEmpty())
        {
            Optional<GroupRepresentation> adminGroup = groups.stream()
                                                             .filter(groupRepresentation -> groupRepresentation.getPath()
                                                                                                               .contains(
                                                                                                                       "ADMIN"))
                                                             .findFirst();

            if (adminGroup.isPresent())
            {
                return adminGroup.get().getName();
            }
        }

        return null;
    }

    @Override
    public String getGroupPath(String groupId)
    {
        GroupRepresentation group = keycloakAuthService.getKeycloakInstance()
                                                       .realm(realm)
                                                       .groups()
                                                       .group(groupId)
                                                       .toRepresentation();
        return group.getPath();
    }

    @Override
    public List<GroupRepresentation> getParentGroups(String groupId)
    {
        RealmResource realmResource = keycloakAuthService.getKeycloakInstance().realm(realm);
        GroupRepresentation group = realmResource.groups().group(groupId).toRepresentation();

        List<GroupRepresentation> parents = new ArrayList<>();
        String[] pathParts = group.getPath().split("/");

        StringBuilder currentPath = new StringBuilder();
        for (String part : pathParts)
        {
            if (!part.isEmpty())
            {
                currentPath.append("/").append(part);
                realmResource.groups().groups().stream()
                             .filter(g -> g.getPath().equals(currentPath.toString()))
                             .findFirst()
                             .ifPresent(parents::add);
            }
        }
        return parents;
    }

    @Override
    public List<GroupRepresentation> getChildGroups(String groupId)
    {
        return keycloakAuthService.getKeycloakInstance()
                                  .realm(realm)
                                  .groups()
                                  .group(groupId)
                                  .getSubGroups(0, KeycloakUtils.getMaxResults(), false);
    }

    @Override
    public void syncGroupHierarchy()
    {
        RealmResource realmResource = keycloakAuthService.getKeycloakInstance().realm(realm);
        Map<String, List<GroupRepresentation>> hierarchy = buildGroupHierarchy(realmResource.groups().groups());
        updateLocalGroupHierarchy(hierarchy);
    }

    @Override
    public Map<String, List<GroupRepresentation>> getCompleteGroupHierarchy()
    {
        RealmResource realmResource = keycloakAuthService.getKeycloakInstance().realm(realm);
        return buildGroupHierarchy(realmResource.groups().groups());
    }

    private Map<String, List<GroupRepresentation>> buildGroupHierarchy(List<GroupRepresentation> groups)
    {
        Map<String, List<GroupRepresentation>> hierarchy = new HashMap<>();
        for (GroupRepresentation group : groups)
        {
            hierarchy.put(group.getId(), getChildGroups(group.getId()));
            processSubGroups(group.getId(), hierarchy);
        }
        return hierarchy;
    }

    private void processSubGroups(String groupId, Map<String, List<GroupRepresentation>> hierarchy)
    {
        List<GroupRepresentation> subGroups = getChildGroups(groupId);
        for (GroupRepresentation subGroup : subGroups)
        {
            hierarchy.put(subGroup.getId(), getChildGroups(subGroup.getId()));
            processSubGroups(subGroup.getId(), hierarchy);
        }
    }

    @Override
    public List<GroupRepresentation> getAllUserGroups(String userId)
    {
        RealmResource realmResource = keycloakAuthService.getKeycloakInstance().realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        List<GroupRepresentation> allGroups = new ArrayList<>();
        List<GroupRepresentation> directGroups = userResource.groups();

        for (GroupRepresentation group : directGroups)
        {
            allGroups.add(group);
            allGroups.addAll(getParentGroups(group.getId()));
        }

        return allGroups.stream().distinct().collect(Collectors.toList());
    }

    private void updateLocalGroupHierarchy(Map<String, List<GroupRepresentation>> hierarchy)
    {
        //TODO: Will take up in future
    }

    private void printGroup(GroupRepresentation group, int level)
    {
        logger.info("{}{} (ID: {})", "  ".repeat(level), group.getName(), group.getId());
        List<GroupRepresentation> subGroups = keycloakUtils.getSubGroups(
                keycloakAuthService.getKeycloakInstance().realm(realm),
                group.getId());
        for (GroupRepresentation subGroup : subGroups)
        {
            printGroup(subGroup, level + 1);
        }
    }
}