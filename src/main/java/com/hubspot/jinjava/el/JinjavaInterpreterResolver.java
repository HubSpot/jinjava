package com.hubspot.jinjava.el;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.PropertyNotFoundException;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.objects.PyWrapper;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.date.FormattedDate;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import com.hubspot.jinjava.util.JinjavaPropertyNotResolvedException;
import de.odysseus.el.util.SimpleResolver;

public class JinjavaInterpreterResolver extends SimpleResolver {

  private JinjavaInterpreter interpreter;

  public JinjavaInterpreterResolver(JinjavaInterpreter interpreter) {
    this.interpreter = interpreter;
  }

  @Override
  public Object invoke(ELContext context, Object base, Object method,
      Class<?>[] paramTypes, Object[] params) {

    try {
      Object methodProperty = getValue(context, base, method, false);
      if (methodProperty != null && methodProperty instanceof AbstractCallableMethod) {
        context.setPropertyResolved(true);
        return ((AbstractCallableMethod) methodProperty).evaluate(params);
      }
    } catch (IllegalArgumentException e) {
      // failed to access property, continue with method calls
    }

    // TODO map named params to special arg in fn to invoke
    return super.invoke(context, base, method, paramTypes, params);
  }

  @Override
  public Object getValue(ELContext context, Object base, Object prop) {
    return getValue(context, base, prop, true);
  }

  private Object getValue(ELContext context, Object base, Object prop, boolean errOnUnknownProp) {
    String property = Objects.toString(prop, "");
    Object value = null;

    if (ExtendedParser.INTERPRETER.equals(prop)) {
      value = interpreter;
    } else if (property.startsWith(ExtendedParser.FILTER_PREFIX)) {
      value = interpreter.getContext().getFilter(StringUtils.substringAfter(property, ExtendedParser.FILTER_PREFIX));
    } else if (property.startsWith(ExtendedParser.EXPTEST_PREFIX)) {
      value = interpreter.getContext().getExpTest(StringUtils.substringAfter(property, ExtendedParser.EXPTEST_PREFIX));
    } else {
      if (base == null) {
        value = interpreter.retraceVariable((String) prop, interpreter.getLineNumber());
      } else {
        try {
          value = super.getValue(context, base, transformName(property));
        } catch (PropertyNotFoundException | JinjavaPropertyNotResolvedException e) {
          if (errOnUnknownProp) {
            interpreter.addError(TemplateError.fromUnknownProperty(base, property, interpreter.getLineNumber()));
          }
        }
      }
    }

    context.setPropertyResolved(true);
    return wrap(interpreter, value);
  }

  @SuppressWarnings("unchecked")
  public static Object wrap(JinjavaInterpreter interpreter, Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof PyWrapper) {
      return value;
    }

    if (List.class.isAssignableFrom(value.getClass())) {
      return new PyList((List<Object>) value);
    }
    if (Map.class.isAssignableFrom(value.getClass())) {
      return new PyMap((Map<String, Object>) value);
    }

    if (Date.class.isAssignableFrom(value.getClass())) {
      return new PyishDate(localizeDateTime(interpreter, ZonedDateTime.ofInstant(Instant.ofEpochMilli(((Date) value).getTime()), ZoneOffset.UTC)));
    }
    if (ZonedDateTime.class.isAssignableFrom(value.getClass())) {
      return new PyishDate(localizeDateTime(interpreter, (ZonedDateTime) value));
    }

    if (FormattedDate.class.isAssignableFrom(value.getClass())) {
      return formattedDateToString(interpreter, (FormattedDate) value);
    }

    return value;
  }

  private static ZonedDateTime localizeDateTime(JinjavaInterpreter interpreter, ZonedDateTime dt) {
    ENGINE_LOG.debug("Using timezone: {} to localize datetime: {}", interpreter.getConfig().getTimeZone(), dt);
    return dt.withZoneSameInstant(interpreter.getConfig().getTimeZone());
  }

  private static String formattedDateToString(JinjavaInterpreter interpreter, FormattedDate d) {
    DateTimeFormatter formatter = getFormatter(interpreter, d).withLocale(getLocale(interpreter, d));
    return formatter.format(localizeDateTime(interpreter, d.getDate()));
  }

  private static DateTimeFormatter getFormatter(JinjavaInterpreter interpreter, FormattedDate d) {
    if (!StringUtils.isBlank(d.getFormat())) {
      try {
        return StrftimeFormatter.formatter(d.getFormat());
      } catch (IllegalArgumentException e) {
        interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.SYNTAX_ERROR, e.getMessage(), null, interpreter.getLineNumber(), null));
      }
    }

    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
  }

  private static Locale getLocale(JinjavaInterpreter interpreter, FormattedDate d) {
    if (!StringUtils.isBlank(d.getLanguage())) {
      try {
        return LocaleUtils.toLocale(d.getLanguage());
      } catch (IllegalArgumentException e) {
        interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.SYNTAX_ERROR, e.getMessage(), null, interpreter.getLineNumber(), null));
      }
    }

    return Locale.US;
  }

  // snake case stuff

  private static final Pattern SNAKE_CASE = Pattern.compile("_([^_]?)");

  private String transformName(String name) {
    Matcher m = SNAKE_CASE.matcher(name);

    StringBuffer result = new StringBuffer(name.length());
    while (m.find()) {
      String replacement = m.group(1).toUpperCase();
      m.appendReplacement(result, replacement);
    }
    m.appendTail(result);

    return result.toString();
  }

}
