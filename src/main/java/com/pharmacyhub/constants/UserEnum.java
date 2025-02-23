package com.pharmacyhub.constants;

public enum UserEnum
{
  PHARMACIST("p"),
  PROPRIETOR("pr"),
  PHARMACY_MANAGER("pm"),
  SALESMAN("s");

  private String userEnum;

  UserEnum(String userEnum) {
    this.userEnum = userEnum;
  }

  public String getUserEnum() {
    return userEnum;
  }
}
