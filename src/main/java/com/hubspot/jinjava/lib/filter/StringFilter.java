package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class StringFilter implements Filter {

  @Override
  public String getName() {
    return "string";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return Objects.toString(var);
  }

}
