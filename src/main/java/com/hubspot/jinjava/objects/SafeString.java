package com.hubspot.jinjava.objects;

public class SafeString {

  private final String value;

  public SafeString(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
