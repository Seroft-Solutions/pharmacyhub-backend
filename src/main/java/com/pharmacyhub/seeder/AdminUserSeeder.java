package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.Role;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
