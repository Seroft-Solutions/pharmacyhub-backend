package com.pharmacyhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.connections.PharmacistsConnections;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.PharmacistRepository;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.repository.connections.PharmacistsConnectionsRepository;
import com.pharmacyhub.security.JwtHelper;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class PharmacistControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacistRepository pharmacistRepository;

    @Autowired
    private PharmacistsConnectionsRepository pharmacistsConnectionsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private JwtHelper jwtHelper;

    private User testUser;
    private User pharmacistUser;
    private Pharmacist pharmacist;
    private Role adminRole;
    private Permission viewPharmacistPermission;
    private Permission createPharmacistPermission;
    private Permission manageConnectionsPermission;

    @BeforeEach
    void setUp() {
        // Clear repositories
        pharmacistsConnectionsRepository.deleteAll();
        pharmacistRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create permissions
        createPharmacistPermission = Permission.builder()
                .name("CREATE_PHARMACIST")
                .description("Permission to create pharmacist")
                .resourceType(ResourceType.PHARMACIST)
                .operationType(OperationType.CREATE)
                .requiresApproval(false)
                .build();
        permissionRepository.save(createPharmacistPermission);
        
        viewPharmacistPermission = Permission.builder()
                .name("VIEW_PHARMACIST")
                .description("Permission to view pharmacist")
                .resourceType(ResourceType.PHARMACIST)
                .operationType(OperationType.READ)
                .requiresApproval(false)
                .build();
        permissionRepository.save(viewPharmacistPermission);
        
        manageConnectionsPermission = Permission.builder()
                .name("MANAGE_CONNECTIONS")
                .description("Permission to manage connections")
                .resourceType(ResourceType.PHARMACIST)
                .operationType(OperationType.MANAGE)
                .requiresApproval(false)
                .build();
        permissionRepository.save(manageConnectionsPermission);
        
        // Create roles
        adminRole = TestDataBuilder.createRole(RoleEnum.ADMIN, 1);
        Set<Permission> permissions = new HashSet<>();
        permissions.add(createPharmacistPermission);
        permissions.add(viewPharmacistPermission);
        permissions.add(manageConnectionsPermission);
        adminRole.setPermissions(permissions);
        roleRepository.save(adminRole);
        
        // Create test users
        testUser = TestDataBuilder.createUser("test@pharmacyhub.pk", "password", UserType.ADMIN);
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);
        
        pharmacistUser = TestDataBuilder.createUser("pharmacist@pharmacyhub.pk", "password", UserType.PHARMACIST);
        pharmacistUser = userRepository.save(pharmacistUser);
        
        // Create test pharmacist
        pharmacist = TestDataBuilder.createPharmacist(pharmacistUser);
        pharmacist = pharmacistRepository.save(pharmacist);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddUserInfo() throws Exception {
        // Create pharmacist DTO
        PharmacistDTO pharmacistDTO = new PharmacistDTO();
        pharmacistDTO.setCategoryAvailable("Yes");
        pharmacistDTO.setLicenseDuration("2 years");
        pharmacistDTO.setExperience("3 years");
        pharmacistDTO.setCity("Karachi");
        pharmacistDTO.setLocation("DHA");
        pharmacistDTO.setUniversityName("LUMS");
        pharmacistDTO.setBatch("F18");
        pharmacistDTO.setContactNumber("03001234567");
        
        // Add user info
        mockMvc.perform(post("/api/pharmacist/v1/add-info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pharmacistDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllPharmacists() throws Exception {
        // Get all pharmacists
        MvcResult result = mockMvc.perform(get("/api/pharmacist/v1/get-all"))
                .andExpect(status().isOk())
                .andReturn();
        
        // Parse response
        List<UserDisplayDTO> pharmacists = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserDisplayDTO.class));
        
        // Verify response
        assertEquals(1, pharmacists.size());
        assertNotNull(pharmacists.get(0).getPharmacist());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConnectWithPharmacist() throws Exception {
        // Create connection DTO
        PHUserConnectionDTO connectionDTO = new PHUserConnectionDTO();
        connectionDTO.setConnectWith(pharmacist.getId());
        
        // Connect with pharmacist
        mockMvc.perform(post("/api/pharmacist/v1/connect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectionDTO)))
                .andExpect(status().isOk());
        
        // Verify connection was created
        List<PharmacistsConnections> connections = pharmacistsConnectionsRepository.findAll();
        assertEquals(1, connections.size());
        assertEquals(pharmacist.getId(), connections.get(0).getPharmacist().getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDisconnectWithPharmacist() throws Exception {
        // Create connection
        PharmacistsConnections connection = new PharmacistsConnections();
        connection.setUser(testUser);
        connection.setPharmacist(pharmacist);
        connection.setState(StateEnum.READY_TO_CONNECT);
        connection = pharmacistsConnectionsRepository.save(connection);
        
        // Create connection DTO
        PHUserConnectionDTO connectionDTO = new PHUserConnectionDTO();
        connectionDTO.setConnectWith(pharmacist.getId());
        
        // Disconnect with pharmacist
        mockMvc.perform(put("/api/pharmacist/v1/disconnect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectionDTO)))
                .andExpect(status().isOk());
        
        // Verify connection state was updated
        connection = pharmacistsConnectionsRepository.findById(connection.getId()).get();
        assertEquals(StateEnum.CLIENT_DISCONNECT, connection.getState());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateConnectionState() throws Exception {
        // Create connection
        PharmacistsConnections connection = new PharmacistsConnections();
        connection.setUser(testUser);
        connection.setPharmacist(pharmacist);
        connection.setState(StateEnum.READY_TO_CONNECT);
        connection = pharmacistsConnectionsRepository.save(connection);
        
        // Create connection DTO
        PHUserConnectionDTO connectionDTO = new PHUserConnectionDTO();
        connectionDTO.setId(connection.getId());
        connectionDTO.setState(StateEnum.DONE);
        
        // Update connection state
        mockMvc.perform(put("/api/pharmacist/v1/update-connection-state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectionDTO)))
                .andExpect(status().isOk());
        
        // Verify state was updated
        connection = pharmacistsConnectionsRepository.findById(connection.getId()).get();
        assertEquals(StateEnum.DONE, connection.getState());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateConnectionNotes() throws Exception {
        // Create connection
        PharmacistsConnections connection = new PharmacistsConnections();
        connection.setUser(testUser);
        connection.setPharmacist(pharmacist);
        connection.setState(StateEnum.READY_TO_CONNECT);
        connection = pharmacistsConnectionsRepository.save(connection);
        
        // Create connection DTO
        PHUserConnectionDTO connectionDTO = new PHUserConnectionDTO();
        connectionDTO.setId(connection.getId());
        connectionDTO.setNotes("Test connection notes");
        
        // Update connection notes
        mockMvc.perform(put("/api/pharmacist/v1/update-connection-notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectionDTO)))
                .andExpect(status().isOk());
        
        // Verify notes were updated
        connection = pharmacistsConnectionsRepository.findById(connection.getId()).get();
        assertEquals("Test connection notes", connection.getNotes());
    }

    @Test
    @WithMockUser(username = "user")
    void testUnauthorizedAccess() throws Exception {
        // Attempt to get all pharmacists without proper permissions - should return 403
        mockMvc.perform(get("/api/pharmacist/v1/get-all"))
                .andExpect(status().isForbidden());
    }
}
