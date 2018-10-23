package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.hubspot.jinjava.objects.date.InvalidDateFormatException;

public class TodayFunctionTest {

  @Test
  public void itDefaultsToUtcTimezone() {
    ZonedDateTime zonedDateTime = Functions.today();
    assertThat(zonedDateTime.getZone()).isEqualTo(ZoneOffset.UTC);
  }

  @Test
  public void itParsesTimezones() {
    ZonedDateTime zonedDateTime = Functions.today("America/New_York");
    assertThat(zonedDateTime.getZone()).isEqualTo(ZoneId.of("America/New_York"));
  }

  @Test(expected = InvalidDateFormatException.class)
  public void itThrowsExceptionOnInvalidTimezone() {
    ZonedDateTime zonedDateTime = Functions.today("Not a timezone");
  }
}
