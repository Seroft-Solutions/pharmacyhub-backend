package com.pharmacyhub.security;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.service.RBACService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtHelper
{

  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private RBACService rbacService;

  @Value("${pharmacyhub.security.jwt.token-validity-in-seconds:18000}")
  private long tokenValidityInSeconds;

  @Value("${pharmacyhub.security.jwt.secret:pharmacyhub_jwt_secret_key_for_token_generation_and_validation_2025}")
  private String secret;

  //retrieve username from jwt token
  public String getUsernameFromToken(String token)
  {
    return getClaimFromToken(token, Claims::getSubject);
  }

  //retrieve expiration date from jwt token
  public Date getExpirationDateFromToken(String token)
  {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver)
  {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  //for retrieveing any information from token we will need the secret key
  protected Claims getAllClaimsFromToken(String token)
  {
    return Jwts.parserBuilder()
            .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  //check if the token has expired
  private Boolean isTokenExpired(String token)
  {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  //generate token for user
  public String generateToken(UserDetails userDetails)
  {
    Map<String, Object> claims = new HashMap<>();
    
    // Add custom claims for roles and permissions
    if (userDetails instanceof User) {
        addRolesAndPermissionsToClaims(claims, (User) userDetails);
    } else {
        // Fallback for non-User implementations
        User user = userRepository.findByEmailAddress(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
        addRolesAndPermissionsToClaims(claims, user);
    }
    
    return doGenerateToken(claims, userDetails.getUsername());
  }
  
  private void addRolesAndPermissionsToClaims(Map<String, Object> claims, User user) {
      // Add user ID
      claims.put("userId", user.getId());
      
      // Add user type
      if (user.getUserType() != null) {
          claims.put("userType", user.getUserType().name());
      }
      
      // Add user profile info
      claims.put("firstName", user.getFirstName());
      claims.put("lastName", user.getLastName());
      claims.put("emailAddress", user.getEmailAddress());
      claims.put("contactNumber", user.getContactNumber());
      claims.put("verified", user.isVerified());
      claims.put("registered", user.isRegistered());
      claims.put("openToConnect", user.isOpenToConnect());
      
      // Get roles and create a list of role names
      Set<Role> userRoles = rbacService.getUserRoles(user.getId());
      List<String> roleNames = userRoles.stream()
          .map(Role::getName)
          .collect(Collectors.toList());
      claims.put("roles", roleNames);
      
      // Get user permissions and create a list of permission names
      Set<Permission> permissions = rbacService.getUserEffectivePermissions(user.getId());
      List<String> permissionNames = permissions.stream()
          .map(Permission::getName)
          .collect(Collectors.toList());
      claims.put("permissions", permissionNames);
      
      // Add Spring Security compatible authorities
      Set<String> authorities = new HashSet<>();
      
      // Add role-based authorities (ROLE_XXX format for Spring Security)
      roleNames.forEach(role -> authorities.add("ROLE_" + role));
      
      // Add permissions directly as authorities
      authorities.addAll(permissionNames);
      
      claims.put("authorities", authorities);
  }

  //while creating the token -
  //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
  //2. Sign the JWT using the HS512 algorithm and secret key.
  //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
  //   compaction of the JWT to a URL-safe string
  private String doGenerateToken(Map<String, Object> claims, String subject)
  {
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + tokenValidityInSeconds * 1000))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes()), io.jsonwebtoken.SignatureAlgorithm.HS512)
            .compact();
  }

  //validate token
  public Boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = getUsernameFromToken(token);
      
      // Validate username and token expiration
      if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
        return false;
      }
      
      // Additional checks for user status when userDetails is our User class
      if (userDetails instanceof User) {
        User user = (User) userDetails;
        
        // Check if user is enabled and account not locked
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
          return false;
        }
        
        // TODO: Check if user is verified (optional, depending on your requirements)
//        if (!user.isVerified()) {
//          return false;
//        }
      }
      
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Extract user permissions from token
   */
  @SuppressWarnings("unchecked")
  public List<String> getPermissionsFromToken(String token) {
      final Claims claims = getAllClaimsFromToken(token);
      return claims.get("permissions", List.class);
  }
  
  /**
   * Extract user roles from token
   */
  @SuppressWarnings("unchecked")
  public List<String> getRolesFromToken(String token) {
      final Claims claims = getAllClaimsFromToken(token);
      return claims.get("roles", List.class);
  }
  
  /**
   * Extract user ID from token
   */
  public Long getUserIdFromToken(String token) {
      final Claims claims = getAllClaimsFromToken(token);
      return claims.get("userId", Long.class);
  }
  
  /**
   * Get all authorities from token (roles and permissions)
   */
  @SuppressWarnings("unchecked")
  public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
      final Claims claims = getAllClaimsFromToken(token);
      List<String> authorities = claims.get("authorities", List.class);
      
      if (authorities == null) {
          return Collections.emptyList();
      }
      
      return authorities.stream()
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
  }
}