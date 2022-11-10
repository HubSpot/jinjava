package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;

public class FormatDatetimeFilterTest {
  private static final ZonedDateTime DATE_TIME = ZonedDateTime.of(
    2022,
    11,
    10,
    17,
    49,
    7,
    0,
    ZoneId.of("America/New_York")
  );
  Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(FormatDatetimeFilter.class);
  }

  @Test
  public void itFormatsNumbers() {
    assertThat(
        jinjava.render("{{ d | format_datetime }}", ImmutableMap.of("d", 1668120547000L))
      )
      .isEqualTo("Nov 10, 2022, 5:49:07 PM");
  }

  @Test
  public void itFormatsPyishDates() {
    PyishDate pyishDate = new PyishDate(1668120547000L);

    assertThat(
        jinjava.render("{{ d | format_datetime }}", ImmutableMap.of("d", pyishDate))
      )
      .isEqualTo("Nov 10, 2022, 5:49:07 PM");
  }

  @Test
  public void itFormatsZonedDateTime() {
    assertThat(
        jinjava.render("{{ d | format_datetime }}", ImmutableMap.of("d", DATE_TIME))
      )
      .isEqualTo("Nov 10, 2022, 5:49:07 PM");
  }

  @Test
  public void itUsesShortFormat() {
    assertThat(
        jinjava.render(
          "{{ d | format_datetime('short') }}",
          ImmutableMap.of("d", DATE_TIME)
        )
      )
      .isEqualTo("11/10/22, 5:49 PM");
  }

  @Test
  public void itUsesLongFormat() {
    assertThat(
        jinjava.render(
          "{{ d | format_datetime('long') }}",
          ImmutableMap.of("d", DATE_TIME)
        )
      )
      .isEqualTo("November 10, 2022 at 5:49:07 PM EST");
  }
}
