package com.hubspot.jinjava.objects.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
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
  private static final String[] CONVERSIONS = new String[255];
  private static final String[] NOMINATIVE_CONVERSIONS = new String[255];

  static {
    CONVERSIONS['a'] = "EEE";
    CONVERSIONS['A'] = "EEEE";
    CONVERSIONS['b'] = "MMM";
    CONVERSIONS['B'] = "MMMM";
    CONVERSIONS['c'] = "EEE MMM dd HH:mm:ss yyyy";
    CONVERSIONS['d'] = "dd";
    CONVERSIONS['e'] = "d"; // The day of the month like with %d, but padded with blank (range 1 through 31).
    CONVERSIONS['f'] = "SSSSSS";
    CONVERSIONS['H'] = "HH";
    CONVERSIONS['h'] = "hh";
    CONVERSIONS['I'] = "hh";
    CONVERSIONS['j'] = "DDD";
    CONVERSIONS['k'] = "H"; // The hour as a decimal number, using a 24-hour clock like %H, but padded with blank (range 0 through 23).
    CONVERSIONS['l'] = "h"; // The hour as a decimal number, using a 12-hour clock like %I, but padded with blank (range 1 through 12).
    CONVERSIONS['m'] = "MM";
    CONVERSIONS['M'] = "mm";
    CONVERSIONS['p'] = "a";
    CONVERSIONS['S'] = "ss";
    CONVERSIONS['U'] = "ww";
    CONVERSIONS['w'] = "e";
    CONVERSIONS['W'] = "ww";
    CONVERSIONS['x'] = "MM/dd/yy";
    CONVERSIONS['X'] = "HH:mm:ss";
    CONVERSIONS['y'] = "yy";
    CONVERSIONS['Y'] = "yyyy";
    CONVERSIONS['z'] = "Z";
    CONVERSIONS['Z'] = "z";
    CONVERSIONS['%'] = "%";

    NOMINATIVE_CONVERSIONS['B'] = "LLLL";
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
      if (c == '%' && strftime.length() > i + 1) {
        c = strftime.charAt(++i);
        boolean stripLeadingZero = false;
        String[] conversions = CONVERSIONS;

        if (c == '-') {
          stripLeadingZero = true;
          c = strftime.charAt(++i);
        }

        if (c == 'O') {
          c = strftime.charAt(++i);
          conversions = NOMINATIVE_CONVERSIONS;
        }

        if (c > 255) {
          throw new InvalidDateFormatException(strftime);
        } else {
          if (stripLeadingZero) {
            builder.appendPattern(conversions[c].substring(1));
          } else {
            builder.appendPattern(conversions[c]);
          }
        } // < 255
      } else if (Character.isLetter(c)) {
        while (Character.isLetter(c)) {
          builder.appendLiteral(c);
          if (++i < strftime.length()) {
            c = strftime.charAt(i);
          } else {
            c = 0;
          }
        }
        --i; // re-consume last char
      } else {
        builder.appendLiteral(c);
      }
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
