package com.hubspot.jinjava.objects.date;

public class CurrentDateTimeProvider implements DateTimeProvider {

  @Override
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }
}
