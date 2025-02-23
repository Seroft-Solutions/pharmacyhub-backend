package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.constants.UserEnum;
import com.pharmacyhub.entity.Role;
import com.pharmacyhub.entity.Salesman;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.repository.SalesmanRepository;
import com.pharmacyhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class SalesmanSeeder
{
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
                  user.setLastName("Salesman");
                  user.setEmailAddress("user" + i + "@pharmacyhub.pk");
                  user.setPassword(passwordEncoder.encode("user" + i));
                  user.setRole(optionalRole.get());
                  user.setRegistered(true);
                  user.setUserType(UserEnum.SALESMAN.getUserEnum());
                  userRepository.save(user);

                  Salesman pharmacist = Salesman.builder()
                          .contactNumber("03456142607")
                          .area("Valencia")
                          .city("FSD")
                          .experience("Lahore")
                          .previousPharmacyName("Test pharmacy")
                          .currentJobStatus("Free")
                          .shiftTime("Morning")
                          .user(user)
                          .build();

                  salesmanRepository.save(pharmacist);
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