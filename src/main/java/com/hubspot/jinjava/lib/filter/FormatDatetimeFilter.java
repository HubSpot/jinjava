package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class FormatDatetimeFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.MEDIUM)
      .format((LocalDateTime) var);
  }

  @Override
  public String getName() {
    return "format_datetime";
  }
}
