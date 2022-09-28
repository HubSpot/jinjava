package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public interface StrftimeConversionComponent {
  char getSourcePattern();
  DateTimeFormatterBuilder append(DateTimeFormatterBuilder builder);
}
