package com.pharmacyhub.engine;

import com.pharmacyhub.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class PHEngine
{
  public User getLoggedInUser()
  {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return (User) userDetails;
  }
}
