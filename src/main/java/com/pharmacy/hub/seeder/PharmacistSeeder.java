package com.pharmacy.hub.seeder;

import com.pharmacy.hub.constants.RoleEnum;
import com.pharmacy.hub.constants.UserEnum;
import com.pharmacy.hub.entity.Pharmacist;
import com.pharmacy.hub.entity.Role;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.repository.PharmacistRepository;
import com.pharmacy.hub.repository.RoleRepository;
import com.pharmacy.hub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class PharmacistSeeder
{
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
                  user.setId(i+"@pharmacit");
                  user.setFirstName("User " + i);
                  user.setLastName("Pharmacist");
                  user.setEmailAddress(email);
                  user.setPassword(passwordEncoder.encode("user" + i));
                  user.setRole(optionalRole.get());
                  user.setRegistered(true);
                  user.setUserType(UserEnum.PHARMACIST.getUserEnum());
                  userRepository.save(user);

                  Pharmacist pharmacist = Pharmacist.builder()
                          .categoryAvailable("Yes")
                          .licenseDuration("1 year")
                          .experience("Yes")
                          .city("Lahore")
                          .area("NFC")
                          .universityName("UCP")
                          .batch("F16")
                          .timePreference("Morning")
                          .previousPharmacyName("Pharmacy")
                          .currentJobStatus("Employed")
                          .contactNumber("03456142607")
                          .user(user)
                          .build();

                  pharmacistRepository.save(pharmacist);

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