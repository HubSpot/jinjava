package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.InvalidDateFormatException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Optional;

public class FormatDatetimeFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    String format = arg(args, 0).orElse("medium");
    ZoneId zoneId = arg(args, 1)
      .map(FormatDatetimeFilter::parseZone)
      .orElse(ZoneOffset.UTC);
    Locale locale = arg(args, 2)
      .map(FormatDatetimeFilter::parseLocale)
      .orElseGet(
        () ->
          JinjavaInterpreter
            .getCurrentMaybe()
            .map(JinjavaInterpreter::getConfig)
            .map(JinjavaConfig::getLocale)
            .orElse(Locale.ENGLISH)
      );

    return buildFormatter(format)
      .withLocale(locale)
      .format(Functions.getDateTimeArg(var, zoneId));
  }

  @Override
  public String getName() {
    return "format_datetime";
  }

  private static Optional<String> arg(String[] args, int index) {
    return args.length > index ? Optional.ofNullable(args[index]) : Optional.empty();
  }

  private static ZoneId parseZone(String zone) {
    try {
      return ZoneId.of(zone);
    } catch (DateTimeException e) {
      throw new InvalidArgumentException(
        JinjavaInterpreter.getCurrent(),
        "format_datetime",
        "Invalid time zone: " + zone
      );
    }
  }

  private static Locale parseLocale(String locale) {
    try {
      return new Locale.Builder().setLanguageTag(locale).build();
    } catch (IllformedLocaleException e) {
      throw new InvalidArgumentException(
        JinjavaInterpreter.getCurrent(),
        "format_datetime",
        "Invalid locale: " + locale
      );
    }
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
