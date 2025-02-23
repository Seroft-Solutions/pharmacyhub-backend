package com.pharmacy.hub.seeder;

import com.pharmacy.hub.constants.RoleEnum;
import com.pharmacy.hub.constants.UserEnum;
import com.pharmacy.hub.entity.Proprietor;
import com.pharmacy.hub.entity.Role;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.repository.ProprietorRepository;
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
public class ProprietorSeeder
{
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
                  user.setLastName("Proprietor");
                  user.setEmailAddress("user" + i + "@pharmacyhub.pk");
                  user.setPassword(passwordEncoder.encode("user" + i));
                  user.setRole(optionalRole.get());
                  user.setRegistered(true);
                  user.setUserType(UserEnum.PROPRIETOR.getUserEnum());
                  userRepository.save(user);

                  Proprietor proprietor = Proprietor.builder()
                          .categoryRequired("Yes")
                          .licenseDuration("2 Year")
                          .experienced("Yes")
                          .pharmacyName("Pharmacy Cool")
                          .city("Lahore")
                          .location("Rehmanpura")
                          .contactNumber("03456142607")
                          .categoryProvince("Punjab")
                          .user(user)
                          .build();

                  proprietorRepository.save(proprietor);

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