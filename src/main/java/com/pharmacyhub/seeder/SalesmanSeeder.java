package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.SalesmanRepository;
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
public class SalesmanSeeder {
    @Value("#{'${pharmacyhub.test.data.salesman}'.split('-')}")
    private List<Integer> range;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SalesmanRepository salesmanRepository;

    public void loadUsers() {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        IntStream.rangeClosed(range.get(0), range.get(1))
                .forEach(i -> {
                    try {
                        String email = "salesman" + i + "@pharmacyhub.pk";

                        if (userRepository.findByEmailAddress(email).isEmpty()) {
                            User user = new User();
                            user.setFirstName("User " + i);
                            user.setLastName("Salesman");
                            user.setEmailAddress(email);
                            user.setPassword(passwordEncoder.encode("salesman" + i));
                            user.setRole(optionalRole.get());
                            user.setRegistered(true);
                            user.setUserType(UserType.SALESMAN);
                            userRepository.save(user);

                            Salesman salesman = Salesman.builder()
                                    .city("Lahore")
                                    .area("NFC")
                                    .contactNumber("03456142607")
                                    .experience("2 years")
                                    .previousPharmacyName("ABC Pharmacy")
                                    .currentJobStatus("Active")
                                    .shiftTime("Morning")
                                    .user(user)
                                    .build();

                            salesmanRepository.save(salesman);
                        }
                    } catch (Exception e) {
                        // Handle the exception appropriately (e.g., log it)
                        e.printStackTrace();
                    }
                });
    }
}
