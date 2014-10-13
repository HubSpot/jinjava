package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class CapitalizeFilter implements Filter {

  @Override
  public String getName() {
    return "capitalize";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var instanceof String) {
      String value = (String) var;
      return StringUtils.capitalize(value);
    }
    return var;
  }

}
