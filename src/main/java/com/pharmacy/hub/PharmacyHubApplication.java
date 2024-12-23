package com.pharmacy.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class PharmacyHubApplication
{
  public static void main(String[] args)
  {
    SpringApplication.run(PharmacyHubApplication.class, args);
  }

}
