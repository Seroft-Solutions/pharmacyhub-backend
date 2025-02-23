package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminUserSeeder {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void loadUsers() {
        loadAdmin();
        loadSuperAdmin();
    }

    private void loadAdmin() {
        try {
            String email = "admin@pharmacyhub.pk";
            Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);

            if (userRepository.findByEmailAddress(email).isEmpty()) {
                User user = new User();
                user.setFirstName("Admin");
                user.setLastName("User");
                user.setEmailAddress(email);
                user.setPassword(passwordEncoder.encode("admin"));
                user.setRole(optionalRole.get());
                user.setRegistered(true);
                user.setUserType(UserType.ADMIN);
                userRepository.save(user);
            }
        } catch (Exception e) {
            // Handle the exception appropriately (e.g., log it)
            e.printStackTrace();
        }
    }

    public void loadSuperAdmin() {
        try {
            String email = "superadmin@pharmacyhub.pk";
            Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);

            if (userRepository.findByEmailAddress(email).isEmpty()) {
                User user = new User();
                user.setFirstName("Super");
                user.setLastName("Admin");
                user.setEmailAddress(email);
                user.setPassword(passwordEncoder.encode("superadmin"));
                user.setRole(optionalRole.get());
                user.setRegistered(true);
                user.setUserType(UserType.SUPER_ADMIN);
                userRepository.save(user);
            }
        } catch (Exception e) {
            // Handle the exception appropriately (e.g., log it)
            e.printStackTrace();
        }
    }
}
