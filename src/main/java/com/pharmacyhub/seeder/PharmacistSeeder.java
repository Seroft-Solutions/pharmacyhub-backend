package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.Pharmacist;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.PharmacistRepository;
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
public class PharmacistSeeder {
    @Value("#{'${pharmacyhub.test.data.pharmacist}'.split('-')}")
    private List<Integer> range;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PharmacistRepository pharmacistRepository;

    public void loadUsers() {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        IntStream.rangeClosed(range.get(0), range.get(1))
                .forEach(i -> {
                    try {
                        String email = "user" + i + "@pharmacyhub.pk";

                        if (userRepository.findByEmailAddress(email).isEmpty()) {
                            User user = new User();
                            user.setFirstName("User " + i);
                            user.setLastName("Pharmacist");
                            user.setEmailAddress(email);
                            user.setPassword(passwordEncoder.encode("user" + i));
                            user.setRole(optionalRole.get());
                            user.setRegistered(true);
                            user.setUserType(UserType.PHARMACIST);
                            userRepository.save(user);

                            Pharmacist pharmacist = Pharmacist.builder()
                                    .categoryAvailable("Yes")
                                    .licenseDuration("1 year")
                                    .experience("Yes")
                                    .city("Lahore")
                                    .location("NFC")
                                    .universityName("UCP")
                                    .batch("F16")
                                    .contactNumber("03456142607")
                                    .categoryProvince("")
                                    .user(user)
                                    .build();

                            pharmacistRepository.save(pharmacist);
                        }
                    } catch (Exception e) {
                        // Handle the exception appropriately (e.g., log it)
                        e.printStackTrace();
                    }
                });
    }
}
