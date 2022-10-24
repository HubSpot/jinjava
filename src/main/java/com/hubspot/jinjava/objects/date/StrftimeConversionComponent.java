package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public interface StrftimeConversionComponent {
  DateTimeFormatterBuilder append(
    DateTimeFormatterBuilder builder,
    boolean stripLeadingZero
  );

  static StrftimeConversionComponent pattern(String targetPattern) {
    return (builder, stripLeadingZero) ->
      builder.appendPattern(
        stripLeadingZero ? targetPattern.substring(1) : targetPattern
      );
  }
}
