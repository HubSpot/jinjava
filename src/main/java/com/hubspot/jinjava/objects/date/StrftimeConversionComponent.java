package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public interface StrftimeConversionComponent {
  char getSourcePattern();
  DateTimeFormatterBuilder append(
    DateTimeFormatterBuilder builder,
    boolean stripLeadingZero
  );

  static PatternStrftimeConversionComponent pattern(
    char sourcePattern,
    String targetPattern
  ) {
    return new PatternStrftimeConversionComponent(sourcePattern, targetPattern);
  }
}
