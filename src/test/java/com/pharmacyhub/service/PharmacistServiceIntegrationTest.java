package com.pharmacyhub.service;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.engine.PHMapper;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.connections.PharmacistsConnections;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.PharmacistRepository;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.repository.connections.PharmacistsConnectionsRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.util.TestDataBuilder;
import com.pharmacyhub.util.TestSecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PharmacistServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PharmacistService pharmacistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacistRepository pharmacistRepository;

    @Autowired
    private PharmacistsConnectionsRepository pharmacistsConnectionsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PHMapper phMapper;

    private User testUser;
    private User pharmacistUser1;
    private User pharmacistUser2;
    private Pharmacist pharmacist1;
    private Pharmacist pharmacist2;

    @BeforeEach
    void setUp() {
        // Clear repositories
        pharmacistsConnectionsRepository.deleteAll();
        pharmacistRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create roles
        Role userRole;
        if (roleRepository.findByName(RoleEnum.USER).isEmpty()) {
            userRole = TestDataBuilder.createRole(RoleEnum.USER, 5);
            userRole = roleRepository.save(userRole);
        } else {
            userRole = roleRepository.findByName(RoleEnum.USER).get();
        }
        
        // Create test users
        testUser = TestDataBuilder.createUser("test@pharmacyhub.pk", "password", UserType.PHARMACIST);
        testUser.setRole(userRole);
        testUser = userRepository.save(testUser);
        
        pharmacistUser1 = TestDataBuilder.createUser("pharmacist1@pharmacyhub.pk", "password", UserType.PHARMACIST);
        pharmacistUser1.setRole(userRole);
        pharmacistUser1 = userRepository.save(pharmacistUser1);
        
        pharmacistUser2 = TestDataBuilder.createUser("pharmacist2@pharmacyhub.pk", "password", UserType.PHARMACIST);
        pharmacistUser2.setRole(userRole);
        pharmacistUser2 = userRepository.save(pharmacistUser2);
        
        // Create test pharmacists
        pharmacist1 = TestDataBuilder.createPharmacist(pharmacistUser1);
        pharmacist1 = pharmacistRepository.save(pharmacist1);
        
        pharmacist2 = TestDataBuilder.createPharmacist(pharmacistUser2);
        pharmacist2 = pharmacistRepository.save(pharmacist2);
    }

    @Test
    void testSaveUser() {
        // Set security context to test user
        TestSecurityUtils.setSecurityContext(testUser);
        
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
        
        // Save pharmacist
        PharmacistDTO savedPharmacist = (PharmacistDTO) pharmacistService.saveUser(pharmacistDTO);
        
        // Verify pharmacist was saved
        assertNotNull(savedPharmacist);
        
        // Verify user is now registered as a pharmacist
        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertTrue(updatedUser.isRegistered());
        assertEquals(UserType.PHARMACIST, updatedUser.getUserType());
        
        // Verify pharmacist entity was created
        Optional<Pharmacist> savedEntity = pharmacistRepository.findById(savedPharmacist.getId());
        assertTrue(savedEntity.isPresent());
        assertEquals("Karachi", savedEntity.get().getCity());
        assertEquals("LUMS", savedEntity.get().getUniversityName());
        
        // Clean up security context
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void testFindAllUsers() {
        // Set security context to test user
        TestSecurityUtils.setSecurityContext(testUser);
        
        // Find all pharmacists
        List<UserDisplayDTO> pharmacists = pharmacistService.findAllUsers();
        
        // Verify pharmacists were found
        assertEquals(2, pharmacists.size());
        
        // Clean up security context
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void testConnectAndDisconnect() {
        // Set security context to test user
        TestSecurityUtils.setSecurityContext(testUser);
        
        // Create connection DTO
        PHUserConnectionDTO connectionDTO = new PHUserConnectionDTO();
        connectionDTO.setConnectWith(pharmacist1.getId());
        
        // Connect with pharmacist
        pharmacistService.connectWith(connectionDTO);
        
        // Verify connection was created
        List<PharmacistsConnections> connections = pharmacistsConnectionsRepository.findByUserAndPharmacistAndState(
                testUser, pharmacist1, StateEnum.READY_TO_CONNECT);
        assertEquals(1, connections.size());
        
        // Get user connections
        List<UserDisplayDTO> userConnections = pharmacistService.getAllUserConnections();
        assertEquals(1, userConnections.size());
        
        // Disconnect with pharmacist
        pharmacistService.disconnectWith(connectionDTO);
        
        // Verify connection state was updated
        connections = pharmacistsConnectionsRepository.findByUserAndPharmacistAndState(
                testUser, pharmacist1, StateEnum.CLIENT_DISCONNECT);
        assertEquals(1, connections.size());
        
        // Clean up security context
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void testUpdateConnectionState() {
        // Set security context to test user
        TestSecurityUtils.setSecurityContext(testUser);
        
        // Create connection
        PharmacistsConnections connection = new PharmacistsConnections();
        connection.setUser(testUser);
        connection.setPharmacist(pharmacist1);
        connection.setState(StateEnum.READY_TO_CONNECT);
        connection = pharmacistsConnectionsRepository.save(connection);
        
        // Create connection DTO for updating state
        PHUserConnectionDTO connectionDTO = new PHUserConnectionDTO();
        connectionDTO.setId(connection.getId());
        connectionDTO.setState(StateEnum.DONE);
        
        // Update connection state
        pharmacistService.updateState(connectionDTO);
        
        // Verify state was updated
        connection = pharmacistsConnectionsRepository.findById(connection.getId()).get();
        assertEquals(StateEnum.DONE, connection.getState());
        
        // Clean up security context
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void testUpdateConnectionNotes() {
        // Set security context to test user
        TestSecurityUtils.setSecurityContext(testUser);
        
        // Create connection
        PharmacistsConnections connection = new PharmacistsConnections();
        connection.setUser(testUser);
        connection.setPharmacist(pharmacist1);
        connection.setState(StateEnum.READY_TO_CONNECT);
        connection = pharmacistsConnectionsRepository.save(connection);
        
        // Create connection DTO for updating notes
        PHUserConnectionDTO connectionDTO = new PHUserConnectionDTO();
        connectionDTO.setId(connection.getId());
        connectionDTO.setNotes("Test connection notes");
        
        // Update connection notes
        pharmacistService.updateNotes(connectionDTO);
        
        // Verify notes were updated
        connection = pharmacistsConnectionsRepository.findById(connection.getId()).get();
        assertEquals("Test connection notes", connection.getNotes());
        
        // Clean up security context
        TestSecurityUtils.clearSecurityContext();
    }
}
