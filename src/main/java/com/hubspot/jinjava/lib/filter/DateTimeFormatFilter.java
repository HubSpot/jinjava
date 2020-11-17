package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import java.util.Map;

@JinjavaDoc(
  value = "Formats a date object",
  input = @JinjavaParam(
    value = "value",
    defaultValue = "current time",
    desc = "The date variable or UNIX timestamp to format",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = DateTimeFormatFilter.FORMAT_PARAM,
      defaultValue = StrftimeFormatter.DEFAULT_DATE_FORMAT,
      desc = "The format of the date determined by the directives added to this parameter"
    ),
    @JinjavaParam(
      value = DateTimeFormatFilter.TIMEZONE_PARAM,
      defaultValue = "UTC",
      desc = "Time zone of output date"
    ),
    @JinjavaParam(
      value = DateTimeFormatFilter.LOCALE_PARAM,
      type = "string",
      defaultValue = "en-US",
      desc = "The language code to use when formatting the datetime"
    )
  },
  snippets = {
    @JinjavaSnippet(code = "{% content.updated|datetimeformat('%B %e, %Y') %}"),
    @JinjavaSnippet(
      code = "{% content.updated|datetimeformat('%a %A %w %d %e %b %B %m %y %Y %H %I %k %l %p %M %S %f %z %Z %j %U %W %c %x %X %%') %}"
    )
  }
)
public class DateTimeFormatFilter extends AbstractFilter implements Filter {
  public static final String FORMAT_PARAM = "format";
  public static final String TIMEZONE_PARAM = "timezone";
  public static final String LOCALE_PARAM = "locale";

  @Override
  public String getName() {
    return "datetimeformat";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    String format = (String) parsedArgs.get(FORMAT_PARAM);
    String timezone = (String) parsedArgs.get(TIMEZONE_PARAM);
    String locale = (String) parsedArgs.get(LOCALE_PARAM);
    if (format == null && timezone == null && locale == null) {
      return Functions.dateTimeFormat(var);
    } else {
      return Functions.dateTimeFormat(var, format, timezone, locale);
    }
  }
}
