package com.pharmacyhub.repository;

import com.pharmacyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
  Optional<User> findByEmailAddress(String emailAddress);
  Optional<User> findByVerificationToken(String token);
  Optional<User> findById(Long id);
  
  /**
   * Find all users that have the specified role name.
   */
  @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
  List<User> findByRolesName(@Param("roleName") String roleName);
  
  /**
   * Find all users that belong to the specified group.
   */
  @Query("SELECT u FROM User u JOIN u.groups g WHERE g.name = :groupName")
  List<User> findByGroupsName(@Param("groupName") String groupName);
  
  /**
   * Find users with a specific permission (either directly through roles or through groups).
   */
  @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.name = :permissionName " +
         "UNION " +
         "SELECT DISTINCT u FROM User u JOIN u.groups g JOIN g.roles r JOIN r.permissions p WHERE p.name = :permissionName")
  List<User> findByPermission(@Param("permissionName") String permissionName);
  
  /**
   * Check if a user has any active permission overrides.
   */
  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.id = :userId AND SIZE(u.permissionOverrides) > 0")
  boolean hasPermissionOverrides(@Param("userId") Long userId);
}
