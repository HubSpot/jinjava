package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;

public class FormatDatetimeFilterTest {
  Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(FormatDatetimeFilter.class);
  }

  @Test
  public void itFormatsLocalDateTimes() {
    LocalDateTime dateTime = LocalDateTime.of(2022, 11, 10, 17, 49, 7);

    assertThat(
        jinjava.render("{{ d | format_datetime }}", ImmutableMap.of("d", dateTime))
      )
      .isEqualTo("Nov 10, 2022, 5:49:07 PM");
  }

  @Test
  public void itFormatsZonedDateTime() {
    ZonedDateTime zonedDateTime = ZonedDateTime.of(
      2022,
      11,
      10,
      17,
      49,
      7,
      0,
      ZoneId.of("America/New_York")
    );

    assertThat(
        jinjava.render("{{ d | format_datetime }}", ImmutableMap.of("d", zonedDateTime))
      )
      .isEqualTo("Nov 10, 2022, 5:49:07 PM");
  }
}
