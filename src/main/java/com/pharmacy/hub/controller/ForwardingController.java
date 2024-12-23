package com.pharmacy.hub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardingController
{
  @RequestMapping(value = "/{path:[^\\.]*}")
  public String forward()
  {
    return "forward:/";
  }
}