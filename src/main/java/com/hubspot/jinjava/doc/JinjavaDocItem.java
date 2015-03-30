package com.hubspot.jinjava.doc;

public abstract class JinjavaDocItem {

  private final String name;
  private final String desc;
  private final String aliasOf;
  private final JinjavaDocParam[] params;
  
  public JinjavaDocItem(String name, String desc, String aliasOf, JinjavaDocParam... params) {
    this.name = name;
    this.desc = desc;
    this.aliasOf = aliasOf;
    this.params = params;
  }

  public String getName() {
    return name;
  }

  public String getDesc() {
    return desc;
  }

  public String getAliasOf() {
    return aliasOf;
  }
  
  public JinjavaDocParam[] getParams() {
    return params;
  }
  
}
