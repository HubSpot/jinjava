package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class FormatDateFilter implements Filter {
  private static final String NAME = "format_date";

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return null;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
