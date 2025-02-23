package com.pharmacyhub.engine;

import com.pharmacyhub.dto.*;
import com.pharmacyhub.dto.display.ConnectionDisplayDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.entity.*;
import com.pharmacyhub.entity.connections.*;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RoleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PHMapper {
    private final ModelMapper modelMapper = new ModelMapper();
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;

    public PHMapper(RoleRepository roleRepository, 
                   PermissionRepository permissionRepository, 
                   GroupRepository groupRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.groupRepository = groupRepository;
    }

    // Existing mapping methods
    public Proprietor getProprietor(ProprietorDTO proprietorDTO) {
        return modelMapper.map(proprietorDTO, Proprietor.class);
    }

    public ProprietorDTO getProprietorDTO(Proprietor proprietor) {
        return modelMapper.map(proprietor, ProprietorDTO.class);
    }

    public Pharmacist getPharmacist(PharmacistDTO pharmacistDTO) {
        return modelMapper.map(pharmacistDTO, Pharmacist.class);
    }

    public PharmacistDTO getPharmacistDTO(Pharmacist pharmacist) {
        return modelMapper.map(pharmacist, PharmacistDTO.class);
    }

    public UserDTO getUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public User getUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public PharmacyManager getPharmacyManager(PharmacyManagerDTO pharmacyManagerDTO) {
        return modelMapper.map(pharmacyManagerDTO, PharmacyManager.class);
    }

    public PharmacyManagerDTO getPharmacyManagerDTO(PharmacyManager pharmacyManager) {
        return modelMapper.map(pharmacyManager, PharmacyManagerDTO.class);
    }

    public Salesman getSalesman(SalesmanDTO salesmanDTO) {
        return modelMapper.map(salesmanDTO, Salesman.class);
    }

    public SalesmanDTO getSalesmanDTO(Salesman salesman) {
        return modelMapper.map(salesman, SalesmanDTO.class);
    }

    public UserDisplayDTO getUserDisplayDTO(User user) {
        return modelMapper.map(user, UserDisplayDTO.class);
    }

    public ReportingUserDTO getReportingUserDTO(User user) {
        return modelMapper.map(user, ReportingUserDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(ProprietorsConnections connections) {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(SalesmenConnections connections) {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(PharmacistsConnections connections) {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    public ConnectionDisplayDTO getConnectionDisplayDTO(PharmacyManagerConnections connections) {
        return modelMapper.map(connections, ConnectionDisplayDTO.class);
    }

    // New RBAC mapping methods
    public Role getRole(RoleDTO roleDTO) {
        Role role = modelMapper.map(roleDTO, Role.class);
        
        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = roleDTO.getPermissionIds().stream()
                .map(id -> permissionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Permission not found")))
                .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        if (roleDTO.getChildRoleIds() != null) {
            Set<Role> childRoles = roleDTO.getChildRoleIds().stream()
                .map(id -> roleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Role not found")))
                .collect(Collectors.toSet());
            role.setChildRoles(childRoles);
        }

        return role;
    }

    public RoleDTO getRoleDTO(Role role) {
        RoleDTO dto = modelMapper.map(role, RoleDTO.class);
        
        if (role.getPermissions() != null) {
            dto.setPermissionIds(role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet()));
        }

        if (role.getChildRoles() != null) {
            dto.setChildRoleIds(role.getChildRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet()));
        }

        return dto;
    }

    public Permission getPermission(PermissionDTO permissionDTO) {
        return modelMapper.map(permissionDTO, Permission.class);
    }

    public PermissionDTO getPermissionDTO(Permission permission) {
        return modelMapper.map(permission, PermissionDTO.class);
    }

    public Group getGroup(GroupDTO groupDTO) {
        Group group = modelMapper.map(groupDTO, Group.class);
        
        if (groupDTO.getRoleIds() != null) {
            Set<Role> roles = groupDTO.getRoleIds().stream()
                .map(id -> roleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Role not found")))
                .collect(Collectors.toSet());
            group.setRoles(roles);
        }

        return group;
    }

    public GroupDTO getGroupDTO(Group group) {
        GroupDTO dto = modelMapper.map(group, GroupDTO.class);
        
        if (group.getRoles() != null) {
            dto.setRoleIds(group.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet()));
        }

        return dto;
    }
}