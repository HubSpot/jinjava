package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public class PatternStrftimeConversionComponent implements StrftimeConversionComponent {
  private final String targetPattern;

  public PatternStrftimeConversionComponent(String targetPattern) {
    this.targetPattern = targetPattern;
  }

  @Override
  public DateTimeFormatterBuilder append(
    DateTimeFormatterBuilder builder,
    boolean stripLeadingZero
  ) {
    return builder.appendPattern(
      stripLeadingZero ? targetPattern.substring(1) : targetPattern
    );
  }
}
