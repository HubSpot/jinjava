package com.hubspot.jinjava.objects.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

  static {
    CONVERSIONS['a'] = "EEE";
    CONVERSIONS['A'] = "EEEE";
    CONVERSIONS['b'] = "MMM";
    CONVERSIONS['B'] = "MMMM";
    CONVERSIONS['c'] = "EEE MMM dd HH:mm:ss yyyy";
    CONVERSIONS['d'] = "dd";
    CONVERSIONS['e'] = "d"; // The day of the month like with %d, but padded with blank (range 1 through 31).
    CONVERSIONS['f'] = "SSSS";
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
    CONVERSIONS['Z'] = "ZZZ";
    CONVERSIONS['%'] = "%";
  }

  /**
   * Parses a string in python strftime format, returning the equivalent string in java date time format.
   *
   * @param strftime
   * @return date formatted as string
   */
  private static String toJavaDateTimeFormat(String strftime) {
    if (!StringUtils.contains(strftime, '%')) {
      return replaceL(strftime);
    }

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < strftime.length(); i++) {
      char c = strftime.charAt(i);
      if (c == '%') {
        c = strftime.charAt(++i);
        boolean stripLeadingZero = false;

        if (c == '-') {
          stripLeadingZero = true;
          c = strftime.charAt(++i);
        }

        if (stripLeadingZero) {
          result.append(CONVERSIONS[c].substring(1));
        } else {
          result.append(CONVERSIONS[c]);
        }
      } else if (Character.isLetter(c)) {
        result.append("'");
        while (Character.isLetter(c)) {
          result.append(c);
          if (++i < strftime.length()) {
            c = strftime.charAt(i);
          } else {
            c = 0;
          }
        }
        result.append("'");
        --i; // re-consume last char
      } else {
        result.append(c);
      }
    }

    return replaceL(result.toString());
  }

  private static String replaceL(String s) {
    return StringUtils.replaceChars(s, 'L', 'M');
  }

  public static DateTimeFormatter formatter(String strftime) {
    return formatter(strftime, Locale.ENGLISH);
  }

  public static DateTimeFormatter formatter(String strftime, Locale locale) {
    DateTimeFormatter fmt;

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
        fmt = DateTimeFormatter.ofPattern(toJavaDateTimeFormat(strftime));
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
