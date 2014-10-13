package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class FormatFilter implements Filter {

  @Override
  public String getName() {
    return "format";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    String fmt = Objects.toString(var, "");
    return String.format(fmt, (Object[]) args);
  }

}
