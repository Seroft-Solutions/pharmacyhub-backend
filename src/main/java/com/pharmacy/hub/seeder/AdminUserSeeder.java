package com.pharmacy.hub.seeder;

import com.pharmacy.hub.constants.RoleEnum;
import com.pharmacy.hub.entity.Role;
import com.pharmacy.hub.entity.User;
import com.pharmacy.hub.repository.RoleRepository;
import com.pharmacy.hub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminUserSeeder
{
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  

  public void loadSuperAdmin()
  {
    Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);

    try
    {
      User user = new User();
      user.setId("1");
      user.setFirstName("Admin");
      user.setEmailAddress("admin@pharmacyhub.pk");
      user.setPassword(passwordEncoder.encode("Pharmacy@Hub#1234"));
      user.setRole(optionalRole.get());

      userRepository.save(user);
    }
    catch (Exception e)
    {
    }
  }

}
