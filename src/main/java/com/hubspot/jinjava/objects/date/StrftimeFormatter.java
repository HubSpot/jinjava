package com.hubspot.jinjava.objects.date;

import static com.hubspot.jinjava.objects.date.StrftimeConversionComponent.pattern;

import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Datetime format string formatter, supporting both python and java compatible format strings by converting any percent-tokens from python into their java equivalents.
 *
 * @author jstehler
 */
public class StrftimeFormatter {
  public static final String DEFAULT_DATE_FORMAT = "%H:%M / %d-%m-%Y";
  /*
   * Mapped from http://strftime.org/, http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
   */
  private static final Map<Character, StrftimeConversionComponent> COMPONENTS;
  private static final Map<Character, StrftimeConversionComponent> NOMINATIVE_COMPONENTS;

  static {
    COMPONENTS =
      ImmutableMap
        .<Character, StrftimeConversionComponent>builder()
        .put('a', pattern("EEE"))
        .put('A', pattern("EEEE"))
        .put('b', pattern("MMM"))
        .put('B', pattern("MMMM"))
        .put('c', pattern("EEE MMM dd HH:mm:ss yyyy"))
        .put('d', pattern("dd"))
        .put('e', pattern("d")) // The day of the month like with %d, but padded with blank (range 1 through 31).
        .put('f', pattern("SSSSSS"))
        .put('H', pattern("HH"))
        .put('h', pattern("hh"))
        .put('I', pattern("hh"))
        .put('j', pattern("DDD"))
        .put('k', pattern("H")) // The hour as a decimal number, using a 24-hour clock like %H, but padded with blank (range 0 through 23).
        .put('l', pattern("h")) // The hour as a decimal number, using a 12-hour clock like %I, but padded with blank (range 1 through 12).
        .put('m', pattern("MM"))
        .put('M', pattern("mm"))
        .put('p', pattern("a"))
        .put('S', pattern("ss"))
        .put('U', pattern("ww"))
        .put('w', pattern("e"))
        .put('W', pattern("ww"))
        .put('x', pattern("MM/dd/yy"))
        .put('X', pattern("HH:mm:ss"))
        .put('y', pattern("yy"))
        .put('Y', pattern("yyyy"))
        .put('z', pattern("Z"))
        .put('Z', pattern("z"))
        .put('%', (builder, stripLeadingZero) -> builder.appendLiteral("%"))
        .build();

    NOMINATIVE_COMPONENTS =
      ImmutableMap
        .<Character, StrftimeConversionComponent>builder()
        .put('B', pattern("LLLL"))
        .build();
  }

  /**
   * Build a {@link DateTimeFormatter} that matches the given Python <code>strftime</code> pattern.
   *
   * @see <a href="https://strftime.org/">Python <code>strftime</code> cheatsheet</a>
   */
  public static DateTimeFormatter toDateTimeFormatter(String strftime) {
    if (!StringUtils.contains(strftime, '%')) {
      return DateTimeFormatter.ofPattern(strftime);
    }

    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();

    for (int i = 0; i < strftime.length(); i++) {
      char c = strftime.charAt(i);
      if (c != '%' || strftime.length() <= i + 1) {
        builder.appendLiteral(c);
        continue;
      }

      c = strftime.charAt(++i);
      boolean stripLeadingZero = false;
      Map<Character, StrftimeConversionComponent> components = COMPONENTS;

      if (c == '-') {
        stripLeadingZero = true;
        c = strftime.charAt(++i);
      }

      if (c == 'O') {
        c = strftime.charAt(++i);
        components = NOMINATIVE_COMPONENTS;
      }

      Optional
        .ofNullable(components.get(c))
        .orElseThrow(() -> new InvalidDateFormatException(strftime))
        .append(builder, stripLeadingZero);
    }

    return builder.toFormatter();
  }

  private static DateTimeFormatter formatter(String strftime, Locale locale) {
    DateTimeFormatter fmt;

    if (strftime == null) {
      strftime = "";
    }

    switch (strftime.toLowerCase()) {
      case "short":
        fmt = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        break;
      case "medium":
        fmt = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        break;
      case "long":
        fmt = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG);
        break;
      case "full":
        fmt = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
        break;
      default:
        try {
          fmt = toDateTimeFormatter(strftime);
          break;
        } catch (IllegalArgumentException e) {
          throw new InvalidDateFormatException(strftime, e);
        }
    }

    return fmt.withLocale(locale);
  }

  public static String format(ZonedDateTime d) {
    return format(d, DEFAULT_DATE_FORMAT);
  }

  public static String format(ZonedDateTime d, Locale locale) {
    return format(d, DEFAULT_DATE_FORMAT, locale);
  }

  public static String format(ZonedDateTime d, String strftime) {
    return format(d, strftime, Locale.ENGLISH);
  }

  public static String format(ZonedDateTime d, String strftime, Locale locale) {
    return formatter(strftime, locale).format(d);
  }
}
