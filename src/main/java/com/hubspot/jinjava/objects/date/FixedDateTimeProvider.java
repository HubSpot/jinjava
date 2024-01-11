package com.hubspot.jinjava.objects.date;

public class FixedDateTimeProvider implements DateTimeProvider {

  private long currentTimeMillis;

  public FixedDateTimeProvider(long currentTimeMillis) {
    this.currentTimeMillis = currentTimeMillis;
  }

  @Override
  public long getCurrentTimeMillis() {
    return currentTimeMillis;
  }
}
