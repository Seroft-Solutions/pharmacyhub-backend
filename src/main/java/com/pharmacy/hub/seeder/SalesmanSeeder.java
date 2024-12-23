package com.pharmacy.hub.seeder;

import com.pharmacy.hub.constants.RoleEnum;
import com.pharmacy.hub.constants.UserEnum;
import com.pharmacy.hub.entity.Role;
import com.pharmacy.hub.entity.Salesman;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.repository.RoleRepository;
import com.pharmacy.hub.repository.SalesmanRepository;
import com.pharmacy.hub.repository.UserRepository;
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