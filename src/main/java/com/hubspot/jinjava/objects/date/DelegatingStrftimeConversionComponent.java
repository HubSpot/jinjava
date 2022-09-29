package com.hubspot.jinjava.objects.date;

import java.time.format.DateTimeFormatterBuilder;

public class DelegatingStrftimeConversionComponent
  implements StrftimeConversionComponent {
  private final char sourcePattern;
  private final StrftimeConversionComponentDelegate delegate;

  public DelegatingStrftimeConversionComponent(
    char sourcePattern,
    StrftimeConversionComponentDelegate delegate
  ) {
    this.sourcePattern = sourcePattern;
    this.delegate = delegate;
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
    return delegate.append(builder, stripLeadingZero);
  }

  @FunctionalInterface
  interface StrftimeConversionComponentDelegate {
    DateTimeFormatterBuilder append(
      DateTimeFormatterBuilder builder,
      boolean stripLeadingZero
    );
  }
}
