package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;

public class DateFormatFunctionsTest {

  Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjava = new Jinjava();
  }

  @Test
  public void itFormatsDates() {
    assertThat(
      jinjava.render(
        "{{ format_date(d, 'medium') }}",
        ImmutableMap.of("d", ZonedDateTime.of(2022, 11, 28, 16, 30, 4, 0, ZoneOffset.UTC))
      )
    )
      .isEqualTo("Nov 28, 2022");
  }

  @Test
  public void itFormatsTimes() {
    assertThat(
      jinjava.render(
        "{{ format_time(d, 'medium') }}",
        ImmutableMap.of("d", ZonedDateTime.of(2022, 11, 28, 16, 30, 4, 0, ZoneOffset.UTC))
      )
    )
      .isIn("4:30:04 PM", "4:30:04 PM");
  }

  @Test
  public void itFormatsDateTimes() {
    assertThat(
      jinjava.render(
        "{{ format_datetime(d, 'medium') }}",
        ImmutableMap.of("d", ZonedDateTime.of(2022, 11, 28, 16, 30, 4, 0, ZoneOffset.UTC))
      )
    )
      .isIn("Nov 28, 2022, 4:30:04 PM", "Nov 28, 2022, 4:30:04 PM");
  }
}
