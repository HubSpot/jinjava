package com.hubspot.jinjava.testobjects;

public enum SimpleColorEnum {
  RED("red-label"),
  GREEN("green-label");

  private final String label;

  SimpleColorEnum(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
