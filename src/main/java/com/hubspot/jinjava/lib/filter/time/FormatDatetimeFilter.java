package com.hubspot.jinjava.lib.filter.time;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import java.time.format.DateTimeFormatter;

@JinjavaDoc(
  value = "Formats both the date and time components of a date object",
  input = @JinjavaParam(
    value = "value",
    desc = "The date object or Unix timestamp to format",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "format",
      defaultValue = "medium",
      desc = "The format to use. One of 'short', 'medium', 'long', 'full', or a custom pattern following Unicode LDML\nhttps://unicode.org/reports/tr35/tr35-dates.html#Date_Format_Patterns"
    ),
    @JinjavaParam(
      value = "timeZone",
      defaultValue = "UTC",
      desc = "Time zone of the output date in IANA TZDB format\nhttps://data.iana.org/time-zones/tzdb/"
    ),
    @JinjavaParam(
      value = "locale",
      defaultValue = "Locale specified on JinjavaConfig",
      desc = "The locale to use for locale-aware formats"
    )
  },
  snippets = {
    @JinjavaSnippet(code = "{{ content.updated | format_datetime('long') }}"),
    @JinjavaSnippet(
      code = "{{ content.updated | format_datetime('yyyyy.MMMM.dd GGG hh:mm a') }}"
    ),
    @JinjavaSnippet(
      code = "{{ content.updated | format_datetime('medium', 'America/New_York', 'de-DE') }}"
    )
  }
)
public class FormatDatetimeFilter implements Filter {
  private static final String NAME = "format_datetime";
  private static final DateTimeFormatHelper HELPER = new DateTimeFormatHelper(
    NAME,
    DateTimeFormatter::ofLocalizedDateTime
  );

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return HELPER.format(var, interpreter, args);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
