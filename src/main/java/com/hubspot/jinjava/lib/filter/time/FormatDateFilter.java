package com.hubspot.jinjava.lib.filter.time;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

public class FormatDateFilter implements Filter {
  private static final String NAME = "format_date";
  private static final DatetimeFormatHelper

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return null;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
