package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public class MappingStrftimeConversionComponent implements StrftimeConversionComponent {
  private final char sourcePattern;
  private final String targetPattern;

  public MappingStrftimeConversionComponent(char sourcePattern, String targetPattern) {
    this.sourcePattern = sourcePattern;
    this.targetPattern = targetPattern;
  }

  @Override
  public char getSourcePattern() {
    return sourcePattern;
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
