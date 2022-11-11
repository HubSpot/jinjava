package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.InvalidDateFormatException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

public class FormatDatetimeFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    String format = arg(args, 0).orElse("medium");
    ZoneId zoneId = arg(args, 1).map(ZoneId::of).orElse(ZoneOffset.UTC);

    return buildFormatter(format).format(Functions.getDateTimeArg(var, zoneId));
  }

  @Override
  public String getName() {
    return "format_datetime";
  }

  private static Optional<String> arg(String[] args, int index) {
    return args.length > index ? Optional.ofNullable(args[index]) : Optional.empty();
  }

  private static DateTimeFormatter buildFormatter(String format) {
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
        try {
          formatter = DateTimeFormatter.ofPattern(format);
        } catch (IllegalArgumentException e) {
          throw new InvalidDateFormatException(format, e);
        }
        break;
    }
    return formatter;
  }
}
