package com.pharmacy.hub.service;

import com.pharmacy.hub.constants.RoleEnum;
import com.pharmacy.hub.constants.UserEnum;
import com.pharmacy.hub.dto.ChangePasswordDTO;
import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.PharmacistDTO;
import com.pharmacy.hub.dto.PharmacyManagerDTO;
import com.pharmacy.hub.dto.ProprietorDTO;
import com.pharmacy.hub.dto.SalesmanDTO;
import com.pharmacy.hub.dto.UserDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;
import com.pharmacy.hub.engine.PHEngine;
import com.pharmacy.hub.engine.PHMapper;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.PharmacyManager;
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.Role;
import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.keycloak.services.Implementation.KeycloakGroupServiceImpl;
import com.pharmacy.hub.repository.RoleRepository;
import com.pharmacy.hub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
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
    private KeycloakGroupServiceImpl keycloakGroupServiceImpl;
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

            Role role = roleRepository.findByName(RoleEnum.USER).get();
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            user.setPassword(encodedPassword);
            user.setRole(role);
            user.setVerificationToken(UUID.randomUUID().toString());
            user.setTokenCreationDate(LocalDateTime.now());

            User savedUser = userRepository.save(user);
          try
          {
            emailService.sendVerificationEmail(user.getEmailAddress(), user.getVerificationToken());
          }
          catch (MessagingException e)
          {
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
    public boolean isRegisteredUser(String userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            return user.isRegistered();
        }
        return false;
    }

    public boolean isUserRole() {
        return getLoggedInUser().getRole().getName().equals(RoleEnum.USER);
    }

    public String getUserType() {
        return getLoggedInUser().getUserType();
    }

    public List getUsers() {
        return userRepository.findAll();
    }

    public PHUserDTO getUserCompleteInformation() {
        User user = getLoggedInUser();
        UserDTO userDTO = phMapper.getUserDTO(user);

        if (user.getUserType().equals(UserEnum.PHARMACIST.getUserEnum())) {
            return userDTO;

        } else if (user.getUserType().equals(UserEnum.PROPRIETOR.getUserEnum())) {
            Proprietor proprietor = proprietorService.getProprietor();
            ProprietorDTO proprietorDTO = phMapper.getProprietorDTO(proprietor);
            userDTO.setProprietor(proprietorDTO);
            return userDTO;

        } else if (user.getUserType().equals(UserEnum.PHARMACY_MANAGER.getUserEnum())) {
            return userDTO;

        } else if (user.getUserType().equals(UserEnum.SALESMAN.getUserEnum())) {
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
        User user = userRepository.findByEmailAddress(changePasswordDTO.getEmailAddress()).get();
        if (user != null) {
            user.setPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }
    public String getUserGroup(String userId)
    {
        return keycloakGroupServiceImpl.getAllUserGroups(userId).get(0).getName();
    }


    public boolean verifyUser(String token)
    {
        User user = userRepository.findByVerificationToken(token).get();
        if (user == null || user.getTokenCreationDate().plusMinutes(30).isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return true;
    }
}
