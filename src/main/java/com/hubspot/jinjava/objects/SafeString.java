package com.hubspot.jinjava.objects;

import com.fasterxml.jackson.annotation.JsonValue;

public class SafeString {
  private final String value;

  public SafeString(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
