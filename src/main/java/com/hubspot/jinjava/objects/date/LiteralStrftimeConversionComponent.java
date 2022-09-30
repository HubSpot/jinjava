package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public class LiteralStrftimeConversionComponent implements StrftimeConversionComponent {
  private final String literal;

  public LiteralStrftimeConversionComponent(String literal) {
    this.literal = literal;
  }

  @Override
  public DateTimeFormatterBuilder append(
    DateTimeFormatterBuilder builder,
    boolean stripLeadingZero
  ) {
    return builder.appendLiteral(literal);
  }
}
