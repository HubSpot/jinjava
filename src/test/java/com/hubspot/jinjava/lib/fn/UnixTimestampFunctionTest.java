package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import java.time.ZonedDateTime;
import org.junit.Test;

public class UnixTimestampFunctionTest {
  private final ZonedDateTime d = ZonedDateTime.parse(
    "2013-11-06T14:22:12.345+00:00[UTC]"
  );
  private final long epochMilliseconds = d.toEpochSecond() * 1000 + 345;

  @Test
  public void itGetsUnixTimestamps() {
    assertThat(Functions.unixtimestamp())
      .isGreaterThan(0)
      .isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(Functions.unixtimestamp(epochMilliseconds)).isEqualTo(epochMilliseconds);
    assertThat(Functions.unixtimestamp(d)).isEqualTo(epochMilliseconds);
    assertThat(
        Math.abs(
          Functions.unixtimestamp((Object) null) -
          ZonedDateTime.now().toEpochSecond() *
          1000
        )
      )
      .isLessThan(1000);
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
      Functions.unixtimestamp((Object) null);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }
}
