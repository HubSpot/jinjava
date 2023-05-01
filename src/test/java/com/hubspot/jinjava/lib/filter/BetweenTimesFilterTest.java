package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import org.junit.Test;

public class BetweenTimesFilterTest extends BaseJinjavaTest {

  @Test
  public void itGetsDurationBetweenTimes() {
    long timestamp = 1543354954000L;
    long oneDay = 1543441354000L;
    long twoMonths = 1548668554000L;

    Map<String, Object> vars = ImmutableMap.of(
      "begin",
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC),
      "end",
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(oneDay), ZoneOffset.UTC),
      "endMonth",
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(twoMonths), ZoneOffset.UTC)
    );

    assertThat(jinjava.render("{{ begin|between_times(end, 'days') }}", vars))
      .isEqualTo("1");
    assertThat(jinjava.render("{{ begin|between_times(end, 'hours') }}", vars))
      .isEqualTo("24");
    assertThat(jinjava.render("{{ begin|between_times(end, 'minutes') }}", vars))
      .isEqualTo("1440");
    assertThat(jinjava.render("{{ begin|between_times(end, 'seconds') }}", vars))
      .isEqualTo("86400");
    assertThat(jinjava.render("{{ begin|between_times(endMonth, 'months') }}", vars))
      .isEqualTo("2");

    vars =
      ImmutableMap.of(
        "begin",
        new PyishDate(
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
        ),
        "end",
        new PyishDate(
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(oneDay), ZoneOffset.UTC)
        )
      );

    assertThat(jinjava.render("{{ begin|between_times(end, 'days') }}", vars))
      .isEqualTo("1");

    vars = ImmutableMap.of("begin", timestamp, "end", oneDay);
    assertThat(jinjava.render("{{ begin|between_times(end, 'days') }}", vars))
      .isEqualTo("1");
  }
}
