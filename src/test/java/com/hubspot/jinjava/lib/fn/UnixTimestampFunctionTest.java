package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;

import org.junit.Test;

public class UnixTimestampFunctionTest {

  private final ZonedDateTime d = ZonedDateTime.parse("2013-11-06T14:22:00.000+00:00[UTC]");

  @Test
  public void itGetsUnixTimestamps() {
    assertThat(Functions.getUnixTimestamp(d.toEpochSecond() * 1000)).isEqualTo(d.toEpochSecond() * 1000);
    assertThat(Functions.getUnixTimestamp(null)).isEqualTo(ZonedDateTime.now().toEpochSecond() * 1000);
  }

}
