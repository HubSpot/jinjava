package com.hubspot.jinjava.doc;

public class JinjavaDocParam {

  private final String name;
  private final String type;
  private final String desc;
  private final String defaultValue;

  public JinjavaDocParam(String name, String type, String desc, String defaultValue) {
    this.name = name;
    this.type = type;
    this.desc = desc;
    this.defaultValue = defaultValue;
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

}
