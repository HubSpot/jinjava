package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.objects.date.PyishDate;

public class BetweenFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itGetsDurationBetweenTimes() {

    long timestamp = 1543354954000L;
    long oneDay = 1543441354000L;

    Map<String, Object> vars = ImmutableMap.of("begin", ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC),
        "end", ZonedDateTime.ofInstant(Instant.ofEpochMilli(oneDay), ZoneOffset.UTC));

    assertThat(jinjava.render("{{ begin|between(end, 'days') }}", vars)).isEqualTo("1");
    assertThat(jinjava.render("{{ begin|between(end, 'hours') }}", vars)).isEqualTo("24");
    assertThat(jinjava.render("{{ begin|between(end, 'minutes') }}", vars)).isEqualTo("1440");
    assertThat(jinjava.render("{{ begin|between(end, 'seconds') }}", vars)).isEqualTo("86400");

    vars = ImmutableMap.of("begin", new PyishDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)),
        "end", new PyishDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(oneDay), ZoneOffset.UTC)));

    assertThat(jinjava.render("{{ begin|between(end, 'days') }}", vars)).isEqualTo("1");

    vars = ImmutableMap.of("begin", timestamp, "end", oneDay);
    assertThat(jinjava.render("{{ begin|between(end, 'days') }}", vars)).isEqualTo("1");
  }
}
