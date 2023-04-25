package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.date.FixedDateTimeProvider;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Test;

public class TodayFunctionTest extends BaseInterpretingTest {
  private static final String ZONE_NAME = "America/New_York";
  private static final ZoneId ZONE_ID = ZoneId.of(ZONE_NAME);

  @Test
  public void itDefaultsToUtcTimezone() {
    ZonedDateTime zonedDateTime = Functions.today();
    assertThat(zonedDateTime.getZone()).isEqualTo(ZoneOffset.UTC);
  }

  @Test
  public void itUsesFixedDateTimeProvider() {
    long ts = 1233333414223L;

    JinjavaInterpreter.pushCurrent(
      new JinjavaInterpreter(
        new Jinjava(),
        new Context(),
        JinjavaConfig
          .newBuilder()
          .withDateTimeProvider(new FixedDateTimeProvider(ts))
          .build()
      )
    );
    try {
      assertThat(Functions.today(ZONE_NAME))
        .isEqualTo(ZonedDateTime.of(2009, 1, 30, 0, 0, 0, 0, ZONE_ID));
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itParsesTimezones() {
    ZonedDateTime zonedDateTime = Functions.today(ZONE_NAME);
    assertThat(zonedDateTime.getZone()).isEqualTo(ZONE_ID);
  }

  @Test(expected = InvalidArgumentException.class)
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
      Functions.today(ZONE_NAME);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }
}
