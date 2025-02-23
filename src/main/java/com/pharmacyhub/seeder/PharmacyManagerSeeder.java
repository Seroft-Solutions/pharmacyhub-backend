package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.constants.UserEnum;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.Role;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.PharmacyManagerRepository;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class PharmacyManagerSeeder
{
  @Value("#{'${pharmacyhub.test.data.pharmacy.manager}'.split('-')}")
  private List<Integer> range;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private PharmacyManagerRepository pharmacyManagerRepository;


  public void loadUsers()
  {
    Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

    IntStream.rangeClosed(range.get(0), range.get(1))
            .forEach(i -> {
              try
              {
                String email = "user" + i + "@pharmacyhub.pk";

                if (userRepository.findByEmailAddress(email).isEmpty())
                {
                  User user = new User();
                  user.setFirstName("User " + i);
                  user.setLastName("Pharmacy Manager");
                  user.setEmailAddress("user" + i + "@pharmacyhub.pk");
                  user.setPassword(passwordEncoder.encode("user" + i));
                  user.setRole(optionalRole.get());
                  user.setRegistered(true);
                  user.setUserType(UserEnum.PHARMACY_MANAGER.getUserEnum());
                  userRepository.save(user);
                  
                  PharmacyManager pharmacyManager = PharmacyManager.builder()
                          .contactNumber("03456142607")
                          .area("NFC")
                          .city("Lahore")
                          .experience("1 year")
                          .previousPharmacyName("Madina pharmacy")
                          .currentJobStatus("resigned")
                          .shiftTime("morning")
                          .user(user)
                          .build();

                  pharmacyManagerRepository.save(pharmacyManager);

                }
              }
              catch (Exception e)
              {
                // Handle the exception appropriately (e.g., log it)
                e.printStackTrace();
              }
            });
  }


}