package com.hubspot.jinjava.lib.fn;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;
import com.hubspot.jinjava.objects.date.InvalidDateFormatException;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;

public class Functions {

  public static final String STRING_TO_TIME_FUNCTION = "stringToTime";

  public static final int RANGE_LIMIT = 1000;

  @JinjavaDoc(value = "Only usable within blocks, will render the contents of the parent block by calling super.", snippets = {
      @JinjavaSnippet(desc = "This gives back the results of the parent block", code = "{% block sidebar %}\n" +
          "    <h3>Table Of Contents</h3>\n\n" +
          "    ...\n" +
          "    {{ super() }}\n" +
          "{% endblock %}")
  })
  public static String renderSuperBlock() {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(interpreter.getConfig().getMaxOutputSize());

    List<? extends Node> superBlock = interpreter.getContext().getSuperBlock();
    if (superBlock != null) {
      for (Node n : superBlock) {
        result.append(n.render(interpreter));
      }
    }

    return result.toString();
  }

  public static List<Object> immutableListOf(Object... items) {
    return Collections.unmodifiableList(Lists.newArrayList(items));
  }

  @JinjavaDoc(value = "return datetime of beginning of the day", params = {
      @JinjavaParam(value = "timezone", type = "string", defaultValue = "utc", desc = "timezone"),
  })
  public static ZonedDateTime today(String... var) {

    ZoneId zoneOffset = ZoneOffset.UTC;
    if (var.length > 0) {
      String timezone = var[0];
      try {
        zoneOffset = ZoneId.of(timezone);
      } catch (DateTimeException e) {
        throw new InvalidDateFormatException(timezone, e);
      }
    }

    ZonedDateTime dateTime = getDateTimeArg(null, zoneOffset);
    return dateTime.toLocalDate().atStartOfDay(zoneOffset);
  }

  @JinjavaDoc(value = "formats a date to a string", params = {
      @JinjavaParam(value = "var", type = "date", defaultValue = "current time"),
      @JinjavaParam(value = "format", defaultValue = StrftimeFormatter.DEFAULT_DATE_FORMAT),
      @JinjavaParam(value = "timezone", defaultValue = "utc", desc = "Time zone of output date")
  })
  public static String dateTimeFormat(Object var, String... format) {

    ZoneId zoneOffset = ZoneOffset.UTC;

    if (format.length > 1) {
      String timezone = format[1];
      try {
        zoneOffset = ZoneId.of(timezone);
      } catch (DateTimeException e) {
        throw new InvalidDateFormatException(timezone, e);
      }
    } else if (var instanceof ZonedDateTime) {
      zoneOffset = ((ZonedDateTime) var).getZone();
    } else if (var instanceof PyishDate) {
      zoneOffset = ((PyishDate) var).toDateTime().getZone();
    }

    ZonedDateTime d = getDateTimeArg(var, zoneOffset);
    if (d == null) {
      return "";
    }

    Locale locale = JinjavaInterpreter.getCurrentMaybe()
        .map(JinjavaInterpreter::getConfig)
        .map(JinjavaConfig::getLocale)
        .orElse(Locale.ENGLISH);

    if (format.length > 0) {
      return StrftimeFormatter.format(d, format[0], locale);
    } else {
      return StrftimeFormatter.format(d, locale);
    }
  }

  public static ZonedDateTime getDateTimeArg(Object var, ZoneId zoneOffset) {

    ZonedDateTime d = null;

    if (var == null) {
      d = ZonedDateTime.now(zoneOffset);
    } else if (var instanceof Number) {
      d = ZonedDateTime.ofInstant(Instant.ofEpochMilli(((Number) var).longValue()), zoneOffset);
    } else if (var instanceof PyishDate) {
      PyishDate pyishDate = ((PyishDate) var);
      d = pyishDate.toDateTime();
      d = d.withZoneSameInstant(zoneOffset);
    } else if (var instanceof ZonedDateTime) {
      d = (ZonedDateTime) var;
      d = d.withZoneSameInstant(zoneOffset);
    } else if (!ZonedDateTime.class.isAssignableFrom(var.getClass())) {
      throw new InterpretException("Input to function must be a date object, was: " + var.getClass());
    }

    return d;
  }

  @JinjavaDoc(value = "gets the unix timestamp milliseconds value of a datetime", params = {
      @JinjavaParam(value = "var", type = "date", defaultValue = "current time"),
  })
  public static long unixtimestamp(Object var) {
    ZonedDateTime d = getDateTimeArg(var, ZoneOffset.UTC);

    if (d == null) {
      return 0;
    }

    return d.toEpochSecond() * 1000;
  }

  @JinjavaDoc(value = "converts a string and datetime format into a datetime object", params = {
      @JinjavaParam(value = "var", type = "datetimeString", desc = "datetime as string"),
      @JinjavaParam(value = "var", type = "datetimeFormat", desc = "format of the datetime string")
  })
  public static PyishDate stringToTime(String datetimeString, String datetimeFormat) {

    if (datetimeString == null) {
      return null;
    }

    if (datetimeFormat == null) {
      throw new InterpretException(String.format("%s() requires non-null datetime format",
          STRING_TO_TIME_FUNCTION));
    }

    try {
      String convertedFormat = StrftimeFormatter.toJavaDateTimeFormat(datetimeFormat);
      return new PyishDate(ZonedDateTime.parse(datetimeString, DateTimeFormatter.ofPattern(convertedFormat)));
    } catch (DateTimeParseException e) {
      throw new InterpretException(String.format("%s() could not match datetime input %s with datetime format %s",
          STRING_TO_TIME_FUNCTION,
          datetimeString,
          datetimeFormat));

    } catch (IllegalArgumentException e) {
      throw new InterpretException(String.format("%s() requires valid datetime format, was %s",
          STRING_TO_TIME_FUNCTION,
          datetimeFormat));
    }
  }

  private static final int DEFAULT_TRUNCATE_LENGTH = 255;
  private static final String DEFAULT_END = "...";

  @JinjavaDoc(value = "truncates a given string to a specified length", params = {
      @JinjavaParam("s"),
      @JinjavaParam(value = "length", type = "number", defaultValue = "255"),
      @JinjavaParam(value = "end", defaultValue = "...")
  })
  public static Object truncate(Object var, Object... arg) {
    if (var instanceof String) {
      int length = DEFAULT_TRUNCATE_LENGTH;
      boolean killwords = false;
      String ends = DEFAULT_END;

      if (arg.length > 0) {
        try {
          length = Integer.parseInt(Objects.toString(arg[0]));
        } catch (Exception e) {
          ENGINE_LOG.debug("truncate(): error setting length for {}, using default {}", arg[0],
              DEFAULT_TRUNCATE_LENGTH);
        }
      }

      if (arg.length > 1) {
        try {
          killwords = BooleanUtils.toBoolean(Objects.toString(arg[1]));
        } catch (Exception e) {
          ENGINE_LOG.warn("truncate(); error setting killwords for {}", arg[1]);
        }
      }

      if (arg.length > 2) {
        ends = Objects.toString(arg[2]);
      }

      String string = (String) var;

      if (string.length() > length) {
        if (!killwords) {
          length = movePointerToJustBeforeLastWord(length, string);
        }

        return string.substring(0, length) + ends;
      } else {
        return string;
      }
    }

    if (var instanceof SafeString) {
      return new SafeString(truncate(var.toString(), arg).toString());
    }

    return var;
  }

  public static int movePointerToJustBeforeLastWord(int pointer, String s) {
    while (pointer > 0 && pointer < s.length()) {
      if (Character.isWhitespace(s.charAt(--pointer))) {
        break;
      }
    }

    return pointer + 1;
  }

  @JinjavaDoc(value = "<p>Return a list containing an arithmetic progression of integers.</p>" +
      "<p>With one parameter, range will return a list from 0 up to (but not including) the value. " +
      " With two parameters, the range will start at the first value and increment by 1 up to (but not including) the second value. " +
      " The third parameter specifies the step increment.</p> <p>All values can be negative. Impossible ranges will return an empty list.</p>" +
      "<p>Ranges can generate a maximum of " + RANGE_LIMIT + " values.</p>",
      params = {
          @JinjavaParam(value = "start", type = "number", defaultValue = "0"),
          @JinjavaParam(value = "end", type = "number"),
          @JinjavaParam(value = "step", type = "number", defaultValue = "1")})
  public static List<Integer> range(Object arg1, Object... args) {

    List<Integer> result = new ArrayList<>();

    int start = 0;
    int end = 0;
    int step = 1;

    switch (args.length) {
      case 0:
        if (NumberUtils.isNumber(arg1.toString())) {
          end = NumberUtils.toInt(arg1.toString(), RANGE_LIMIT);
        }
        break;
      case 1:
        start = NumberUtils.toInt(arg1.toString());
        if (NumberUtils.isNumber(args[0].toString())) {
          end = NumberUtils.toInt(args[0].toString(), start + RANGE_LIMIT);
        }
        break;
      default:
        start = NumberUtils.toInt(arg1.toString());
        if (NumberUtils.isNumber(args[0].toString())) {
          end = NumberUtils.toInt(args[0].toString(), start + RANGE_LIMIT);
        }
        step = NumberUtils.toInt(args[1].toString(), 1);
    }

    if (step == 0) {
      return result;
    }

    if (start < end) {

      if (step < 0) {
        return result;
      }

      for (int i = start; i < end; i += step) {
        if (result.size() >= RANGE_LIMIT) {
          break;
        }
        result.add(i);
      }
    } else {

      if (step > 0) {
        return result;
      }

      for (int i = start; i > end; i += step) {
        if (result.size() >= RANGE_LIMIT) {
          break;
        }
        result.add(i);
      }
    }

    return result;
  }
}
