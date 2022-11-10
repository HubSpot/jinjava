package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

public class FormatDatetimeFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    String format = arg(args, 0).orElse("medium");

    final DateTimeFormatter formatter;
    switch (format) {
      case "short":
        formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        break;
      case "medium":
        formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        break;
      case "long":
        formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG);
        break;
      case "full":
        formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
        break;
      default:
        throw new RuntimeException("Not yet implemented");
    }

    return formatter.format(Functions.getDateTimeArg(var, ZoneId.of("America/New_York")));
  }

  @Override
  public String getName() {
    return "format_datetime";
  }

  private static Optional<String> arg(String[] args, int index) {
    return args.length > index ? Optional.ofNullable(args[index]) : Optional.empty();
  }
}
