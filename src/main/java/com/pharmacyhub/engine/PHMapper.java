package com.pharmacyhub.engine;

import com.pharmacyhub.dto.*;
import com.pharmacyhub.dto.display.ConnectionDisplayDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.entity.*;
import com.pharmacyhub.entity.connections.PharmacistsConnections;
import com.pharmacyhub.entity.connections.PharmacyManagerConnections;
import com.pharmacyhub.entity.connections.ProprietorsConnections;
import com.pharmacyhub.entity.connections.SalesmenConnections;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PHMapper
{
    private final ModelMapper modelMapper = new ModelMapper();
    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;

    public PHMapper(RolesRepository rolesRepository,
                    PermissionRepository permissionRepository,
                    GroupRepository groupRepository)
    {
        this.rolesRepository = rolesRepository;
        this.permissionRepository = permissionRepository;
        this.groupRepository = groupRepository;
    }

    // Existing mapping methods
    public Proprietor getProprietor(ProprietorDTO proprietorDTO)
    {
        return modelMapper.map(proprietorDTO, Proprietor.class);
    }

    public ProprietorDTO getProprietorDTO(Proprietor proprietor)
    {
        return modelMapper.map(proprietor, ProprietorDTO.class);
    }

    public Pharmacist getPharmacist(PharmacistDTO pharmacistDTO)
    {
        return modelMapper.map(pharmacistDTO, Pharmacist.class);
    }

    public PharmacistDTO getPharmacistDTO(Pharmacist pharmacist)
    {
        return modelMapper.map(pharmacist, PharmacistDTO.class);
    }

    public UserDTO getUserDTO(User user)
    {
        return modelMapper.map(user, UserDTO.class);
    }

    public User getUser(UserDTO userDTO)
    {
        return modelMapper.map(userDTO, User.class);
    }

    public PharmacyManager getPharmacyManager(PharmacyManagerDTO pharmacyManagerDTO)
    {
        return modelMapper.map(pharmacyManagerDTO, PharmacyManager.class);
    }

    public PharmacyManagerDTO getPharmacyManagerDTO(PharmacyManager pharmacyManager)
    {
        return modelMapper.map(pharmacyManager, PharmacyManagerDTO.class);
    }

    public Salesman getSalesman(SalesmanDTO salesmanDTO)
    {
        return modelMapper.map(salesmanDTO, Salesman.class);
    }

    public SalesmanDTO getSalesmanDTO(Salesman salesman)
    {
        return modelMapper.map(salesman, SalesmanDTO.class);
    }

    public UserDisplayDTO getUserDisplayDTO(User user)
    {
        return modelMapper.map(user, UserDisplayDTO.class);
    }

    public ReportingUserDTO getReportingUserDTO(User user)
    {
        return modelMapper.map(user, ReportingUserDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(ProprietorsConnections connections)
    {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(SalesmenConnections connections)
    {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(PharmacistsConnections connections)
    {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(PharmacyManagerConnections connections)
    {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    // Improved RBAC mapping methods with explicit type casting
    public Role getRole(RoleDTO roleDTO)
    {
        if (roleDTO == null) {
            return null;
        }
        
        Role role = modelMapper.map(roleDTO, Role.class);

        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty())
        {
            Set<Permission> permissions = new HashSet<>();
            for (Long id : roleDTO.getPermissionIds()) {
                Permission permission = permissionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        if (roleDTO.getChildRoleIds() != null && !roleDTO.getChildRoleIds().isEmpty())
        {
            Set<Role> childRoles = new HashSet<>();
            for (Long id : roleDTO.getChildRoleIds()) {
                Role childRole = rolesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
                childRoles.add(childRole);
            }
            role.setChildRoles(childRoles);
        }

        return role;
    }

    public RoleDTO getRoleDTO(Role role)
    {
        if (role == null) {
            return null;
        }
        
        RoleDTO dto = modelMapper.map(role, RoleDTO.class);
        
        Set<Long> permissionIds = new HashSet<>();
        Set<Long> childRoleIds = new HashSet<>();

        if (role.getPermissions() != null && !role.getPermissions().isEmpty())
        {
            for (Permission permission : role.getPermissions()) {
                permissionIds.add(permission.getId());
            }
            dto.setPermissionIds(new ArrayList<>(permissionIds));
        }

        if (role.getChildRoles() != null && !role.getChildRoles().isEmpty())
        {
            for (Role childRole : role.getChildRoles()) {
                childRoleIds.add(childRole.getId());
            }
            dto.setChildRoleIds(new ArrayList<>(childRoleIds));
        }

        return dto;
    }

    public Permission getPermission(PermissionDTO permissionDTO)
    {
        if (permissionDTO == null) {
            return null;
        }
        return modelMapper.map(permissionDTO, Permission.class);
    }

    public PermissionDTO getPermissionDTO(Permission permission)
    {
        if (permission == null) {
            return null;
        }
        return modelMapper.map(permission, PermissionDTO.class);
    }

    public Group getGroup(GroupDTO groupDTO)
    {
        if (groupDTO == null) {
            return null;
        }
        
        Group group = modelMapper.map(groupDTO, Group.class);

        if (groupDTO.getRoleIds() != null && !groupDTO.getRoleIds().isEmpty())
        {
            Set<Role> roles = new HashSet<>();
            for (Long id : groupDTO.getRoleIds()) {
                Role role = rolesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
                roles.add(role);
            }
            group.setRoles(roles);
        }

        return group;
    }

    public GroupDTO getGroupDTO(Group group)
    {
        if (group == null) {
            return null;
        }
        
        GroupDTO dto = modelMapper.map(group, GroupDTO.class);
        
        Set<Long> roleIds = new HashSet<>();

        if (group.getRoles() != null && !group.getRoles().isEmpty())
        {
            for (Object roleObj : group.getRoles()) {
                Role role = (Role) roleObj;
                roleIds.add(role.getId());
            }
            dto.setRoleIds(new ArrayList<>(roleIds));
        }

        return dto;
    }
}
