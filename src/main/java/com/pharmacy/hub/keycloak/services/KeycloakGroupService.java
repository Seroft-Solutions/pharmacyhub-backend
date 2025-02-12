package com.pharmacy.hub.keycloak.services;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

//@CacheConfig(cacheNames = "keycloak-groups")
public interface KeycloakGroupService
{

    //@Cacheable(key = "'group-' + #parentGroupId + '-child-' + #childGroupName")
    void createChildGroupIfNotExists(String parentGroupId, String childGroupName);

    //@CacheEvict(allEntries = true)
    void createChildGroupAndAssignUser(String userId, String email);

    //@CacheEvict(allEntries = true)
    void assignUserToChildGroup(String userId, String parentGroupId, String childGroupName);

    //@CacheEvict(allEntries = true)
    void assignUserToBusinessGroup(String userId, String parentGroupId, String businessGroupName);

    //@CacheEvict(allEntries = true)
    void assignUserToGroup(String userId, String groupId);

    //@CacheEvict(allEntries = true)
    void unassignUserToGroup(String userId, String groupId);

    //    @Cacheable(key = "'groups-with-subgroup-' + #subgroupName")
    List<GroupRepresentation> getGroupsContainingSubgroup(String subgroupName);

    //@Cacheable(key = "'group-hierarchy'")
    void printGroupHierarchy();

    //@Cacheable(key = "'user-tenant-' + #email")
    String getUserTenantGroup(String email);

    //@Cacheable(key = "'group-hierarchy'")
    Map<String, List<GroupRepresentation>> getCompleteGroupHierarchy();

    //@Cacheable(key = "'user-groups-' + #userId")
    List<GroupRepresentation> getAllUserGroups(String userId);

    //@CacheEvict(allEntries = true)
    void moveUserToAnotherSubGroup(String userId, String sourceGroupName, String sourceSubGroupName,
                                   String targetGroupName, String targetSubGroupName);

    //@CacheEvict(allEntries = true)
    void addUserToSubGroup(String userId, String groupName, String subGroupName);

    //@CacheEvict(allEntries = true)
    void removeUserFromSubGroup(String userId, String groupName, String subGroupName);

    //@Cacheable(key = "'child-group-check-' + #userId")
    boolean checkAndCreateUserChildGroup(String userId);

    //@Cacheable(key = "'group-path-' + #groupId")
    String getGroupPath(String groupId);

    //@Cacheable(key = "'parent-groups-' + #groupId")
    List<GroupRepresentation> getParentGroups(String groupId);

    //@Cacheable(key = "'child-groups-' + #groupId")
    List<GroupRepresentation> getChildGroups(String groupId);

    //@CacheEvict(allEntries = true)
    void syncGroupHierarchy();
}