package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UnixTimestampFilterTest extends BaseInterpretingTest {
  private final ZonedDateTime d = ZonedDateTime.parse(
    "2013-11-06T14:22:00.000+00:00[UTC]"
  );
  private final String timestamp = Long.toString(d.toEpochSecond() * 1000);

  @Before
  public void setup() {
    interpreter.getContext().put("d", d);
  }

  @After
  public void tearDown() throws Exception {
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itRendersFromDate() throws Exception {
    assertThat(interpreter.renderFlat("{{ d|unixtimestamp }}")).isEqualTo(timestamp);
  }
}
