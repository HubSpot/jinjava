package com.hubspot.jinjava.objects.date;

public class InvalidDateFormatException extends IllegalArgumentException {
  private static final long serialVersionUID = -1577669116818659228L;

  private final String format;

  public InvalidDateFormatException(String format, Throwable t) {
    super("Invalid date format: [" + format + "]", t);
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

}
