package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.date.FixedDateTimeProvider;
import java.time.ZonedDateTime;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Test;

public class UnixTimestampFunctionTest {
  private final ZonedDateTime d = ZonedDateTime.parse(
    "2013-11-06T14:22:12.345+00:00[UTC]"
  );
  private final long epochMilliseconds = d.toEpochSecond() * 1000 + 345;

  @After
  public void tearDown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itGetsUnixTimestamps() {
    JinjavaInterpreter.pushCurrent(
      new JinjavaInterpreter(
        new Jinjava(),
        new Context(),
        JinjavaConfig.newBuilder().build()
      )
    );
    assertThat(Functions.unixtimestamp())
      .isGreaterThan(0)
      .isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(Functions.unixtimestamp(epochMilliseconds)).isEqualTo(epochMilliseconds);
    assertThat(Functions.unixtimestamp(d)).isEqualTo(epochMilliseconds);
    assertThat(Functions.unixtimestamp((Object) null))
      .isCloseTo(System.currentTimeMillis(), Offset.offset(1000L));
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
    assertThat(Functions.unixtimestamp((Object) null)).isEqualTo(ts);
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
    Functions.unixtimestamp((Object) null);
  }
}
