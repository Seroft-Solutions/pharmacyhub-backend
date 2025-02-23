package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.Proprietor;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.ProprietorRepository;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class ProprietorSeeder {
    @Value("#{'${pharmacyhub.test.data.proprietor}'.split('-')}")
    private List<Integer> range;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProprietorRepository proprietorRepository;

    public void loadUsers() {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        IntStream.rangeClosed(range.get(0), range.get(1))
                .forEach(i -> {
                    try {
                        String email = "proprietor" + i + "@pharmacyhub.pk";

                        if (userRepository.findByEmailAddress(email).isEmpty()) {
                            User user = new User();
                            user.setFirstName("User " + i);
                            user.setLastName("Proprietor");
                            user.setEmailAddress(email);
                            user.setPassword(passwordEncoder.encode("proprietor" + i));
                            user.setRole(optionalRole.get());
                            user.setRegistered(true);
                            user.setUserType(UserType.PROPRIETOR);
                            userRepository.save(user);

                            Proprietor proprietor = Proprietor.builder()
                                    .city("Lahore")
                                    .location("NFC")
                                    .contactNumber("03456142607")
                                    .user(user)
                                    .build();

                            proprietorRepository.save(proprietor);
                        }
                    } catch (Exception e) {
                        // Handle the exception appropriately (e.g., log it)
                        e.printStackTrace();
                    }
                });
    }
}
