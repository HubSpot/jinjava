package com.hubspot.jinjava.objects.date;

import org.joda.time.DateTime;

public class FormattedDate {

  private final String format;
  private final String language;
  private final DateTime date;
  
  public FormattedDate(String format, String language, DateTime date) {
    this.format = format;
    this.language = language;
    this.date = date;
  }
  
  public String getFormat() {
    return format;
  }
  public DateTime getDate() {
    return date;
  }
  public String getLanguage() {
    return language;
  }
  
}
