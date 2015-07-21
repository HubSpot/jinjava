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

import javax.el.ArrayELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.MapELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.ResourceBundleELResolver;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.el.ext.JinjavaBeanELResolver;
import com.hubspot.jinjava.el.ext.JinjavaListELResolver;
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

import de.odysseus.el.util.SimpleResolver;

public class JinjavaInterpreterResolver extends SimpleResolver {

  private static final ELResolver DEFAULT_RESOLVER_READ_WRITE = new CompositeELResolver() {
    {
      add(new ArrayELResolver(false));
      add(new JinjavaListELResolver(false));
      add(new MapELResolver(false));
      add(new ResourceBundleELResolver());
      add(new JinjavaBeanELResolver(false));
    }
  };

  private JinjavaInterpreter interpreter;

  public JinjavaInterpreterResolver(JinjavaInterpreter interpreter) {
    super(DEFAULT_RESOLVER_READ_WRITE);
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

  /**
   * {@inheritDoc}
   *
   * If the base object is null, the property will be looked up in the context.
   */
  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    return getValue(context, base, property, true);
  }

  private Object getValue(ELContext context, Object base, Object property, boolean errOnUnknownProp) {
    String propertyName = Objects.toString(property, "");
    Object value = null;

    if (ExtendedParser.INTERPRETER.equals(property)) {
      value = interpreter;
    } else if (propertyName.startsWith(ExtendedParser.FILTER_PREFIX)) {
      value = interpreter.getContext().getFilter(StringUtils.substringAfter(propertyName, ExtendedParser.FILTER_PREFIX));
    } else if (propertyName.startsWith(ExtendedParser.EXPTEST_PREFIX)) {
      value = interpreter.getContext().getExpTest(StringUtils.substringAfter(propertyName, ExtendedParser.EXPTEST_PREFIX));
    } else {
      if (base == null) {
        // Look up property in context.
        value = interpreter.retraceVariable((String) property, interpreter.getLineNumber());
      } else {
        // Get property of base object.
        try {
          value = super.getValue(context, base, propertyName);
        } catch (PropertyNotFoundException e) {
          if (errOnUnknownProp) {
            interpreter.addError(TemplateError.fromUnknownProperty(base, propertyName, interpreter.getLineNumber()));
          }
        }
      }
    }

    context.setPropertyResolved(true);
    return wrap(value);
  }

  @SuppressWarnings("unchecked")
  Object wrap(Object value) {
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

}
