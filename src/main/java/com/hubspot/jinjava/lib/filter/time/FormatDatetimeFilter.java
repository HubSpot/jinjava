package com.hubspot.jinjava.lib.filter.time;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import java.time.format.DateTimeFormatter;

public class FormatDatetimeFilter implements Filter {
  private static final String NAME = "format_datetime";
  private static final DatetimeFormatHelper HELPER = new DatetimeFormatHelper(
    NAME,
    DateTimeFormatter::ofLocalizedDateTime
  );

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return HELPER.format(var, interpreter, args);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
