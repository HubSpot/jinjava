package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.date.InvalidDateFormatException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Test;

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
    Functions.today("Not a timezone");
  }

  @Test
  public void itIgnoresNullTimezone() {
    assertThat(Functions.today((String) null).getZone()).isEqualTo(ZoneOffset.UTC);
  }

  @Test(expected = DeferredValueException.class)
  public void itDefersWhenExecutingEagerly() {
    JinjavaInterpreter.pushCurrent(
      new JinjavaInterpreter(
        new Jinjava(),
        new Context(),
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      )
    );
    try {
      Functions.today("America/New_York");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }
}
