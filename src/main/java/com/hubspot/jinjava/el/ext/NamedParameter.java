package com.hubspot.jinjava.el.ext;

public class NamedParameter {

  private final String name;
  private final Object value;
  
  public NamedParameter(String name, Object value) {
    this.name = name;
    this.value = value;
  }
  
  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return value.toString();
  }
  
}
