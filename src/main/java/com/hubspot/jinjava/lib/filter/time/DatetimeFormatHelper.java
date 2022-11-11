package com.hubspot.jinjava.lib.filter.time;

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
import java.util.function.Function;

final class DatetimeFormatHelper {
  private final String name;
  private final Function<FormatStyle, DateTimeFormatter> cannedFormatterFunction;

  DatetimeFormatHelper(
    String name,
    Function<FormatStyle, DateTimeFormatter> cannedFormatterFunction
  ) {
    this.name = name;
    this.cannedFormatterFunction = cannedFormatterFunction;
  }

  String format(Object var, JinjavaInterpreter interpreter, String... args) {
    String format = arg(args, 0).orElse("medium");
    ZoneId zoneId = arg(args, 1).map(this::parseZone).orElse(ZoneOffset.UTC);
    Locale locale = arg(args, 2)
      .map(this::parseLocale)
      .orElseGet(() -> interpreter.getConfig().getLocale());

    return buildFormatter(format)
      .withLocale(locale)
      .format(Functions.getDateTimeArg(var, zoneId));
  }

  private static Optional<String> arg(String[] args, int index) {
    return args.length > index ? Optional.ofNullable(args[index]) : Optional.empty();
  }

  private ZoneId parseZone(String zone) {
    try {
      return ZoneId.of(zone);
    } catch (DateTimeException e) {
      throw new InvalidArgumentException(
        JinjavaInterpreter.getCurrent(),
        name,
        "Invalid time zone: " + zone
      );
    }
  }

  private Locale parseLocale(String locale) {
    try {
      return new Locale.Builder().setLanguageTag(locale).build();
    } catch (IllformedLocaleException e) {
      throw new InvalidArgumentException(
        JinjavaInterpreter.getCurrent(),
        name,
        "Invalid locale: " + locale
      );
    }
  }

  private DateTimeFormatter buildFormatter(String format) {
    switch (format) {
      case "short":
        return cannedFormatterFunction.apply(FormatStyle.SHORT);
      case "medium":
        return cannedFormatterFunction.apply(FormatStyle.MEDIUM);
      case "long":
        return cannedFormatterFunction.apply(FormatStyle.LONG);
      case "full":
        return cannedFormatterFunction.apply(FormatStyle.FULL);
      default:
        try {
          return DateTimeFormatter.ofPattern(format);
        } catch (IllegalArgumentException e) {
          throw new InvalidDateFormatException(format, e);
        }
    }
  }
}
