package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public interface StrftimeConversionComponent {
  char getSourcePattern();
  DateTimeFormatterBuilder append(DateTimeFormatterBuilder builder);

  static MappingStrftimeConversionComponent mapping(
    char sourcePattern,
    String targetPattern
  ) {
    return new MappingStrftimeConversionComponent(sourcePattern, targetPattern);
  }
}
