package com.pharmacyhub.engine;

import com.pharmacyhub.seeder.AdminUserSeeder;
import com.pharmacyhub.seeder.PharmacistSeeder;
import com.pharmacyhub.seeder.PharmacyManagerSeeder;
import com.pharmacyhub.seeder.ProprietorSeeder;
import com.pharmacyhub.seeder.RoleSeeder;
import com.pharmacyhub.seeder.SalesmanSeeder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SeederEngine implements ApplicationListener<ContextRefreshedEvent>
{
  @Value("${pharmacyhub.seeder.engine}")
  private boolean isEngineEnable;

  @Autowired
  private RoleSeeder roleSeeder;
  @Autowired
  private AdminUserSeeder adminUserSeeder;
  @Autowired
  private PharmacistSeeder pharmacistSeeder;
  @Autowired
  private PharmacyManagerSeeder pharmacyManagerSeeder;
  @Autowired
  private ProprietorSeeder proprietorSeeder;
  @Autowired
  private SalesmanSeeder salesmanSeeder;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event)
  {
    if (isEngineEnable)
    {
      roleSeeder.loadRoles();
      adminUserSeeder.loadSuperAdmin();
      pharmacistSeeder.loadUsers();
      pharmacyManagerSeeder.loadUsers();
      proprietorSeeder.loadUsers();
      salesmanSeeder.loadUsers();
    }
  }
}
