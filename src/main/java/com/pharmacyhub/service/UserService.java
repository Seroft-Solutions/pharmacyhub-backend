package com.pharmacyhub.service;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.dto.ChangePasswordDTO;
import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.PharmacistDTO;
import com.pharmacyhub.dto.PharmacyManagerDTO;
import com.pharmacyhub.dto.ProprietorDTO;
import com.pharmacyhub.dto.SalesmanDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;
import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.engine.PHMapper;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.Proprietor;
import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.mail.MessagingException;

@Service
public class UserService extends PHEngine implements PHUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PHMapper phMapper;
    @Autowired
    private EmailService emailService;

    @Autowired
    private PharmacistService pharmacistService;
    @Autowired
    private ProprietorService proprietorService;
    @Autowired
    private PharmacyManagerService pharmacyManagerService;
    @Autowired
    private SalesmanService salesmanService;

    @Override
    public PHUserDTO saveUser(PHUserDTO phUserDTO) {
        UserDTO userDTO = (UserDTO) phUserDTO;
        if (getUserByEmailAddress(userDTO)==null) {
            User user = phMapper.getUser(userDTO);

            Role role = roleRepository.findByName(RoleEnum.USER).orElseThrow(() ->
                new RuntimeException("Default user role not found"));
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            user.setPassword(encodedPassword);
            user.setRole(role);
            user.setVerificationToken(UUID.randomUUID().toString());
            user.setTokenCreationDate(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            try {
                emailService.sendVerificationEmail(user.getEmailAddress(), user.getVerificationToken());
            } catch (MessagingException e) {
                return null;
            }
            return phMapper.getUserDTO(savedUser);
        }
        return null;
    }

    @Override
    public PHUserDTO updateUser(PHUserDTO phUserDTO) {
        UserDTO userDTO = (UserDTO) phUserDTO;
        User user = phMapper.getUser(userDTO);
        User savedUser = userRepository.save(user);
        return phMapper.getUserDTO(savedUser);
    }

    @Override
    public PHUserDTO findUser(long id) {
        User user = userRepository.findById(id).get();
        return phMapper.getUserDTO(user);
    }

    @Override
    public List<UserDisplayDTO> findAllUsers() {
        return null;
    }

    @Override
    public void connectWith(PHUserConnectionDTO phUserConnectionDTO) {
    }

    @Override
    public void disconnectWith(PHUserConnectionDTO phUserConnectionDTO) {
    }

    @Override
    public List<UserDisplayDTO> getAllUserConnections() {
        return null;
    }

    @Override
    public void updateState(PHUserConnectionDTO userConnectionDTO) {
    }

    @Override
    public void updateNotes(PHUserConnectionDTO userConnectionDTO) {
    }

    @Override
    public List getAllConnections() {
        return null;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public PHUserDTO changeUserPassword(ChangePasswordDTO changePasswordDTO) {
        UserDTO user = phMapper.getUserDTO(getLoggedInUser());

        if (user != null && matchPassword(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
            return updateUser(user);
        }
        return null;
    }

    private boolean matchPassword(String currentPassword, String existingPassword) {
        return passwordEncoder.matches(currentPassword, existingPassword);
    }

    public Boolean updateUserStatus() {
        User user = getLoggedInUser();
        user.setOpenToConnect(!user.isOpenToConnect());
        User savedUser = userRepository.save(user);
        return savedUser != null;
    }

    public boolean isUserRole() {
        Role role = getLoggedInUser().getRole();
        return role != null && role.getName().equals(RoleEnum.USER);
    }

    public UserType getUserType() {
        return getLoggedInUser().getUserType();
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public PHUserDTO getUserCompleteInformation() {
        User user = getLoggedInUser();
        UserDTO userDTO = phMapper.getUserDTO(user);

        if (user.getUserType() == UserType.PHARMACIST) {
            Pharmacist pharmacist = pharmacistService.getPharmacist();
            PharmacistDTO pharmacistDTO = phMapper.getPharmacistDTO(pharmacist);
            userDTO.setPharmacist(pharmacistDTO);
            return userDTO;
        } else if (user.getUserType() == UserType.PROPRIETOR) {
            Proprietor proprietor = proprietorService.getProprietor();
            ProprietorDTO proprietorDTO = phMapper.getProprietorDTO(proprietor);
            userDTO.setProprietor(proprietorDTO);
            return userDTO;
        } else if (user.getUserType() == UserType.PHARMACY_MANAGER) {
            PharmacyManager pharmacyManager = pharmacyManagerService.getPharmacyManager();
            PharmacyManagerDTO pharmacyManagerDTO = phMapper.getPharmacyManagerDTO(pharmacyManager);
            userDTO.setPharmacyManager(pharmacyManagerDTO);
            return userDTO;
        } else if (user.getUserType() == UserType.SALESMAN) {
            Salesman salesman = salesmanService.getSalesman();
            SalesmanDTO salesmanDTO = phMapper.getSalesmanDTO(salesman);
            userDTO.setSalesman(salesmanDTO);
            return userDTO;
        }

        return null;
    }

    public PHUserDTO editUserInformation(UserDTO phUserDTO) {
        getLoggedInUser().setFirstName(phUserDTO.getFirstName());
        getLoggedInUser().setLastName(phUserDTO.getLastName());
        User user = userRepository.save(getLoggedInUser());

        if (phUserDTO.getPharmacist() != null) {
            return pharmacistService.updateUser(phUserDTO.getPharmacist());
        } else if (phUserDTO.getProprietor() != null) {
            return proprietorService.updateUser(phUserDTO.getProprietor());
        } else if (phUserDTO.getPharmacyManager() != null) {
            return pharmacyManagerService.updateUser(phUserDTO.getPharmacyManager());
        } else if (phUserDTO.getSalesman() != null) {
            return salesmanService.updateUser(phUserDTO.getSalesman());
        }

        return null;
    }

    public User getUserByEmailAddress(UserDTO userDTO) {
        Optional<User> byEmailAddress = userRepository.findByEmailAddress(userDTO.getEmailAddress());
        if (byEmailAddress.isPresent()) {
            return byEmailAddress.get();
        }
        return null;
    }

    public boolean forgotPassword(UserDTO changePasswordDTO) {
        Optional<User> userOpt = userRepository.findByEmailAddress(changePasswordDTO.getEmailAddress());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean verifyUser(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty() || userOpt.get().getTokenCreationDate().plusMinutes(30).isBefore(LocalDateTime.now())) {
            return false;
        }
        User user = userOpt.get();
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return true;
    }
}
