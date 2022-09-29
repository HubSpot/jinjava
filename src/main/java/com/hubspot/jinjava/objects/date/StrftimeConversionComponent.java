package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public interface StrftimeConversionComponent {
  DateTimeFormatterBuilder append(
    DateTimeFormatterBuilder builder,
    boolean stripLeadingZero
  );

  static PatternStrftimeConversionComponent pattern(String targetPattern) {
    return new PatternStrftimeConversionComponent(targetPattern);
  }
}
