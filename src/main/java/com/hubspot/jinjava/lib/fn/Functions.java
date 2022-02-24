package com.hubspot.jinjava.lib.fn;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.el.ext.NamedParameter;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.mode.ExecutionMode;
import com.hubspot.jinjava.objects.Namespace;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class Functions {
  public static final String STRING_TO_TIME_FUNCTION = "stringToTime";
  public static final String STRING_TO_DATE_FUNCTION = "stringToDate";

  public static final int DEFAULT_RANGE_LIMIT = 1000;

  @JinjavaDoc(
    value = "Only usable within blocks, will render the contents of the parent block by calling super.",
    snippets = {
      @JinjavaSnippet(
        desc = "This gives back the results of the parent block",
        code = "{% block sidebar %}\n" +
        "    <h3>Table Of Contents</h3>\n\n" +
        "    ...\n" +
        "    {{ super() }}\n" +
        "{% endblock %}"
      )
    }
  )
  public static String renderSuperBlock() {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );

    List<? extends Node> superBlock = interpreter.getContext().getSuperBlock();
    if (superBlock != null) {
      for (Node n : superBlock) {
        result.append(n.render(interpreter));
      }
    }

    return result.toString();
  }

  @SuppressWarnings("unchecked")
  @JinjavaDoc(
    value = "Create a namespace object that can hold arbitrary attributes." +
    "It may be initialized from a dictionary or with keyword arguments.",
    params = {
      @JinjavaParam(
        value = "dictionary",
        type = "Map",
        desc = "The dictionary to initialize with"
      ),
      @JinjavaParam(
        value = "kwargs",
        type = "NamedParameter...",
        desc = "Keyword arguments to put into the namespace dictionary"
      )
    },
    snippets = {
      @JinjavaSnippet(code = "{% set ns = namespace() %}"),
      @JinjavaSnippet(code = "{% set ns = namespace(b=false) %}"),
      @JinjavaSnippet(code = "{% set ns = namespace(my_map, b=false) %}")
    }
  )
  public static Namespace createNamespace(Object... parameters) {
    Namespace namespace = parameters.length > 0 && parameters[0] instanceof Map
      ? new Namespace((Map<String, Object>) parameters[0])
      : new Namespace();
    namespace.putAll(
      Arrays
        .stream(parameters)
        .filter(
          p -> p instanceof NamedParameter && ((NamedParameter) p).getValue() != null
        )
        .map(p -> (NamedParameter) p)
        .collect(Collectors.toMap(NamedParameter::getName, NamedParameter::getValue))
    );
    return namespace;
  }

  public static List<Object> immutableListOf(Object... items) {
    return Collections.unmodifiableList(Lists.newArrayList(items));
  }

  @JinjavaDoc(
    value = "return datetime of beginning of the day",
    params = {
      @JinjavaParam(
        value = "timezone",
        type = "string",
        defaultValue = "utc",
        desc = "timezone"
      )
    }
  )
  public static ZonedDateTime today(String... var) {
    ZoneId zoneOffset = ZoneOffset.UTC;
    if (var.length > 0 && var[0] != null) {
      String timezone = var[0];
      try {
        zoneOffset = ZoneId.of(timezone);
      } catch (DateTimeException e) {
        throw new InvalidArgumentException(
          JinjavaInterpreter.getCurrent(),
          "today",
          String.format("Invalid timezone: %s", timezone)
        );
      }
    }

    ZonedDateTime dateTime = getDateTimeArg(null, zoneOffset);
    return dateTime.toLocalDate().atStartOfDay(zoneOffset);
  }

  @JinjavaDoc(
    value = "formats a date to a string",
    params = {
      @JinjavaParam(value = "var", type = "date", defaultValue = "current time"),
      @JinjavaParam(
        value = "format",
        defaultValue = StrftimeFormatter.DEFAULT_DATE_FORMAT
      ),
      @JinjavaParam(
        value = "timezone",
        defaultValue = "utc",
        desc = "Time zone of output date"
      )
    }
  )
  public static String dateTimeFormat(Object var, String... format) {
    ZoneId zoneOffset = ZoneOffset.UTC;

    if (format.length > 1) {
      String timezone = format[1];
      try {
        zoneOffset = ZoneId.of(timezone);
      } catch (DateTimeException | NullPointerException e) {
        throw new InvalidArgumentException(
          JinjavaInterpreter.getCurrent(),
          "datetimeformat",
          String.format("Invalid timezone: %s", timezone)
        );
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

    Locale locale;
    if (format.length > 2 && format[2] != null) {
      locale = Locale.forLanguageTag(format[2]);
    } else {
      locale =
        JinjavaInterpreter
          .getCurrentMaybe()
          .map(JinjavaInterpreter::getConfig)
          .map(JinjavaConfig::getLocale)
          .orElse(Locale.ENGLISH);
    }

    if (format.length > 0) {
      return StrftimeFormatter.format(d, format[0], locale);
    } else {
      return StrftimeFormatter.format(d, locale);
    }
  }

  public static ZonedDateTime getDateTimeArg(Object var, ZoneId zoneOffset) {
    ZonedDateTime d = null;

    if (var == null) {
      if (
        JinjavaInterpreter
          .getCurrentMaybe()
          .map(JinjavaInterpreter::getConfig)
          .map(JinjavaConfig::getExecutionMode)
          .map(ExecutionMode::useEagerParser)
          .orElse(false)
      ) {
        throw new DeferredValueException(
          "Time is deferred",
          JinjavaInterpreter.getCurrent().getLineNumber(),
          JinjavaInterpreter.getCurrent().getPosition()
        );
      }
      d = ZonedDateTime.now(zoneOffset);
    } else if (var instanceof Number) {
      d =
        ZonedDateTime.ofInstant(
          Instant.ofEpochMilli(((Number) var).longValue()),
          zoneOffset
        );
    } else if (var instanceof PyishDate) {
      PyishDate pyishDate = ((PyishDate) var);
      d = pyishDate.toDateTime();
      d = d.withZoneSameInstant(zoneOffset);
    } else if (var instanceof ZonedDateTime) {
      d = (ZonedDateTime) var;
      d = d.withZoneSameInstant(zoneOffset);
    } else if (!ZonedDateTime.class.isAssignableFrom(var.getClass())) {
      throw new InterpretException(
        "Input to function must be a date object, was: " + var.getClass()
      );
    }

    return d;
  }

  @JinjavaDoc(
    value = "gets the unix timestamp milliseconds value of a datetime",
    params = {
      @JinjavaParam(value = "var", type = "date", defaultValue = "current time")
    }
  )
  public static long unixtimestamp(Object... var) {
    ZonedDateTime d = getDateTimeArg(
      var == null || var.length == 0 ? null : var[0],
      ZoneOffset.UTC
    );

    if (d == null) {
      return 0;
    }

    return d.toEpochSecond() * 1000 + d.getNano() / 1_000_000;
  }

  @JinjavaDoc(
    value = "converts a string and datetime format into a datetime object",
    params = {
      @JinjavaParam(value = "var", type = "datetimeString", desc = "datetime as string"),
      @JinjavaParam(
        value = "var",
        type = "datetimeFormat",
        desc = "format of the datetime string"
      )
    }
  )
  public static PyishDate stringToTime(String datetimeString, String datetimeFormat) {
    if (datetimeString == null) {
      return null;
    }

    if (datetimeFormat == null) {
      throw new InterpretException(
        String.format("%s() requires non-null datetime format", STRING_TO_TIME_FUNCTION)
      );
    }

    try {
      String convertedFormat = StrftimeFormatter.toJavaDateTimeFormat(datetimeFormat);
      return new PyishDate(
        ZonedDateTime.parse(datetimeString, DateTimeFormatter.ofPattern(convertedFormat))
      );
    } catch (DateTimeParseException e) {
      throw new InterpretException(
        String.format(
          "%s() could not match datetime input %s with datetime format %s",
          STRING_TO_TIME_FUNCTION,
          datetimeString,
          datetimeFormat
        )
      );
    } catch (IllegalArgumentException e) {
      throw new InterpretException(
        String.format(
          "%s() requires valid datetime format, was %s",
          STRING_TO_TIME_FUNCTION,
          datetimeFormat
        )
      );
    }
  }

  @JinjavaDoc(
    value = "converts a string and date format into a date object",
    params = {
      @JinjavaParam(value = "dateString", type = "string", desc = "date as string"),
      @JinjavaParam(
        value = "dateFormat",
        type = "string",
        desc = "format of the date string"
      )
    }
  )
  public static PyishDate stringToDate(String dateString, String dateFormat) {
    if (dateString == null) {
      return null;
    }

    if (dateFormat == null) {
      throw new InterpretException(
        String.format("%s() requires non-null date format", STRING_TO_DATE_FUNCTION)
      );
    }

    try {
      String convertedFormat = StrftimeFormatter.toJavaDateTimeFormat(dateFormat);
      return new PyishDate(
        LocalDate
          .parse(dateString, DateTimeFormatter.ofPattern(convertedFormat))
          .atTime(0, 0)
          .toInstant(ZoneOffset.UTC)
      );
    } catch (DateTimeParseException e) {
      throw new InterpretException(
        String.format(
          "%s() could not match date input %s with date format %s",
          STRING_TO_DATE_FUNCTION,
          dateString,
          dateFormat
        )
      );
    } catch (IllegalArgumentException e) {
      throw new InterpretException(
        String.format(
          "%s() requires valid date format, was %s",
          STRING_TO_DATE_FUNCTION,
          dateFormat
        )
      );
    }
  }

  private static final int DEFAULT_TRUNCATE_LENGTH = 255;
  private static final String DEFAULT_END = "...";

  @JinjavaDoc(
    value = "truncates a given string to a specified length",
    params = {
      @JinjavaParam(
        value = "string",
        type = "string",
        desc = "String to be truncated",
        required = true
      ),
      @JinjavaParam(
        value = "length",
        type = "number",
        defaultValue = "255",
        desc = "Specifies the length at which to truncate the text (includes HTML characters)"
      ),
      @JinjavaParam(
        value = "killwords",
        type = "boolean",
        defaultValue = "False",
        desc = "If true, the string will cut text at length"
      ),
      @JinjavaParam(
        value = "end",
        defaultValue = "...",
        desc = "The characters that will be added to indicate where the text was truncated"
      )
    }
  )
  public static Object truncate(Object var, Object... arg) {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();

    if (var instanceof String) {
      int length = DEFAULT_TRUNCATE_LENGTH;
      boolean killwords = false;
      String ends = DEFAULT_END;

      if (arg.length > 0) {
        try {
          length = Integer.parseInt(Objects.toString(arg[0]));
        } catch (Exception e) {
          interpreter.addError(
            TemplateError.fromInvalidArgumentException(
              new InvalidArgumentException(
                interpreter,
                "truncate",
                String.format(
                  "truncate(): error setting length of %s, using default of %d",
                  arg[0],
                  DEFAULT_TRUNCATE_LENGTH
                )
              )
            )
          );
        }
      }

      if (arg.length > 1) {
        try {
          killwords = BooleanUtils.toBoolean(Objects.toString(arg[1]));
        } catch (Exception e) {
          interpreter.addError(
            TemplateError.fromInvalidArgumentException(
              new InvalidArgumentException(
                interpreter,
                "truncate",
                String.format("truncate(): error setting killwords for %s", arg[1])
              )
            )
          );
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
        length = Math.max(length, 0);

        return string.substring(0, length) + ends;
      } else {
        return string;
      }
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

  @JinjavaDoc(
    value = "Return a list containing an arithmetic progression of integers. " +
    "With one parameter, range will return a list from 0 up to (but not including) the value. " +
    "With two parameters, the range will start at the first value and increment by 1 up to (but not including) the second value. " +
    "The third parameter specifies the step increment. All values can be negative. Impossible ranges will return an empty list. " +
    "Ranges can generate a maximum of " +
    DEFAULT_RANGE_LIMIT +
    " values.",
    params = {
      @JinjavaParam(value = "start", type = "number", defaultValue = "0"),
      @JinjavaParam(value = "end", type = "number"),
      @JinjavaParam(value = "step", type = "number", defaultValue = "1")
    }
  )
  public static List<Integer> range(Object arg1, Object... args) {
    int rangeLimit = JinjavaInterpreter.getCurrent().getConfig().getRangeLimit();
    List<Integer> result = new ArrayList<>();

    int start = 0;
    int end = 0;
    int step = 1;

    if (arg1 == null) {
      throw new InvalidArgumentException(
        JinjavaInterpreter.getCurrent(),
        "range",
        "Invalid null passed to range function"
      );
    }

    switch (args.length) {
      case 0:
        if (NumberUtils.isCreatable(arg1.toString())) {
          end = NumberUtils.toInt(arg1.toString(), rangeLimit);
        }
        break;
      case 1:
        start = NumberUtils.toInt(arg1.toString());
        if (args[0] != null && NumberUtils.isCreatable(args[0].toString())) {
          end = NumberUtils.toInt(args[0].toString(), start + rangeLimit);
        }
        break;
      default:
        start = NumberUtils.toInt(arg1.toString());
        if (args[0] != null && NumberUtils.isCreatable(args[0].toString())) {
          end = NumberUtils.toInt(args[0].toString(), start + rangeLimit);
        }
        if (args[1] != null) {
          step = NumberUtils.toInt(args[1].toString(), 1);
        }
    }

    if (step == 0) {
      return result;
    }

    if (start < end) {
      if (step < 0) {
        return result;
      }

      for (int i = start; i < end; i += step) {
        if (result.size() >= rangeLimit) {
          break;
        }
        result.add(i);
      }
    } else {
      if (step > 0) {
        return result;
      }

      for (int i = start; i > end; i += step) {
        if (result.size() >= rangeLimit) {
          break;
        }
        result.add(i);
      }
    }

    return result;
  }
}
