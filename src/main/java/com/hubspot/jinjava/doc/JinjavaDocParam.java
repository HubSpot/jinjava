package com.hubspot.jinjava.doc;

public class JinjavaDocParam {

  private final String name;
  private final String type;
  private final String desc;
  private final String defaultValue;
  private final boolean required;

  public JinjavaDocParam(
    String name,
    String type,
    String desc,
    String defaultValue,
    boolean required
  ) {
    this.name = name;
    this.type = type;
    this.desc = desc;
    this.defaultValue = defaultValue;
    this.required = required;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public boolean getRequired() {
    return required;
  }
}
