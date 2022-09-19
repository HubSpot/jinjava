package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class PlusTimeFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itAddsTime() {
    long timestamp = 1543352736000L;

    long oneDay = 1543439136000L;
    long oneHour = 1543356336000L;
    long oneMinute = 1543352796000L;
    long oneSecond = 1543352737000L;
    long oneMonth = 1545944736000L;
    Map<String, Object> vars = ImmutableMap.of(
      "test",
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
    );
    assertThat(jinjava.render("{{ test|plus_time(1, 'days')|unixtimestamp }}", vars))
      .isEqualTo(String.valueOf(oneDay));
    assertThat(jinjava.render("{{ test|plus_time(1, 'hours')|unixtimestamp }}", vars))
      .isEqualTo(String.valueOf(oneHour));
    assertThat(jinjava.render("{{ test|plus_time(1, 'minutes')|unixtimestamp }}", vars))
      .isEqualTo(String.valueOf(oneMinute));
    assertThat(jinjava.render("{{ test|plus_time(1, 'seconds')|unixtimestamp }}", vars))
      .isEqualTo(String.valueOf(oneSecond));
    assertThat(jinjava.render("{{ test|plus_time(1, 'months')|unixtimestamp }}", vars))
      .isEqualTo(String.valueOf(oneMonth));

    vars =
      ImmutableMap.of(
        "test",
        new PyishDate(
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
        )
      );
    assertThat(jinjava.render("{{ test|plus_time(1, 'days')|unixtimestamp }}", vars))
      .isEqualTo(String.valueOf(oneDay));

    vars = ImmutableMap.of("test", timestamp);
    assertThat(jinjava.render("{{ test|plus_time(1, 'days')|unixtimestamp }}", vars))
      .isEqualTo(String.valueOf(oneDay));
  }

  @Test
  public void itErrorsOnInvalidVariableType() {
    Map<String, Object> vars = ImmutableMap.of("test", "not a date");
    RenderResult renderResult = jinjava.renderForResult(
      "{{ test|plus_time(1, 'days')|unixtimestamp }}",
      vars
    );
    assertThat(renderResult.getErrors()).hasSize(1);
  }

  @Test
  public void itErrorsOnInvalidDelta() {
    long timestamp = 1543352736000L;

    Map<String, Object> vars = ImmutableMap.of(
      "test",
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
    );
    RenderResult renderResult = jinjava.renderForResult(
      "{{ test|plus_time('not a number', 'days')|unixtimestamp }}",
      vars
    );
    assertThat(renderResult.getErrors()).hasSize(1);
  }

  @Test
  public void itErrorsOnInvalidTemporalUnit() {
    long timestamp = 1543352736000L;

    Map<String, Object> vars = ImmutableMap.of(
      "test",
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
    );
    RenderResult renderResult = jinjava.renderForResult(
      "{{ test|plus_time(1, 'parsec')|unixtimestamp }}",
      vars
    );
    assertThat(renderResult.getErrors()).hasSize(1);
  }

  @Test
  public void itWarnsOnDateTimeException() {
    long timestamp = 1543352736000L;

    Map<String, Object> vars = ImmutableMap.of("test", timestamp);
    RenderResult renderResult = jinjava.renderForResult(
      "{{ test|plus_time(9999999999, 'years')|unixtimestamp }}",
      vars
    );
    assertThat(renderResult.getOutput()).isEqualTo(String.valueOf(timestamp));
    assertThat(renderResult.getErrors()).hasSize(1);
  }
}
