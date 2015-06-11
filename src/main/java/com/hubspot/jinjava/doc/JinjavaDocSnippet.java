package com.hubspot.jinjava.doc;

public class JinjavaDocSnippet {

  private final String desc;
  private final String code;
  private final String output;

  public JinjavaDocSnippet(String desc, String code, String output) {
    this.desc = desc;
    this.code = code;
    this.output = output;
  }

  public String getDesc() {
    return desc;
  }

  public String getCode() {
    return code;
  }

  public String getOutput() {
    return output;
  }

}
