package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class FormatDatetimeFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.MEDIUM)
      .format(Functions.getDateTimeArg(var, ZoneId.of("America/New_York")));
  }

  @Override
  public String getName() {
    return "format_datetime";
  }
}
