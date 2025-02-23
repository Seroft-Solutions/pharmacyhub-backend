package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.PharmacyManagerRepository;
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
public class PharmacyManagerSeeder {
    @Value("#{'${pharmacyhub.test.data.pharmacy-manager}'.split('-')}")
    private List<Integer> range;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PharmacyManagerRepository pharmacyManagerRepository;

    public void loadUsers() {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        IntStream.rangeClosed(range.get(0), range.get(1))
                .forEach(i -> {
                    try {
                        String email = "manager" + i + "@pharmacyhub.pk";

                        if (userRepository.findByEmailAddress(email).isEmpty()) {
                            User user = new User();
                            user.setFirstName("User " + i);
                            user.setLastName("Manager");
                            user.setEmailAddress(email);
                            user.setPassword(passwordEncoder.encode("manager" + i));
                            user.setRole(optionalRole.get());
                            user.setRegistered(true);
                            user.setUserType(UserType.PHARMACY_MANAGER);
                            userRepository.save(user);

                            PharmacyManager pharmacyManager = PharmacyManager.builder()
                                    .city("Lahore")
                                    .area("NFC")
                                    .contactNumber("03456142607")
                                    .experience("2 years")
                                    .previousPharmacyName("ABC Pharmacy")
                                    .currentJobStatus("Active")
                                    .shiftTime("Morning")
                                    .user(user)
                                    .build();

                            pharmacyManagerRepository.save(pharmacyManager);
                        }
                    } catch (Exception e) {
                        // Handle the exception appropriately (e.g., log it)
                        e.printStackTrace();
                    }
                });
    }
}
