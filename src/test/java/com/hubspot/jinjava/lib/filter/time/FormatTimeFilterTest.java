package com.hubspot.jinjava.lib.filter.time;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;

public class FormatTimeFilterTest {
  private static final ZonedDateTime DATE_TIME = ZonedDateTime.of(
    2022,
    11,
    10,
    22,
    49,
    7,
    0,
    ZoneOffset.UTC
  );

  Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(FormatTimeFilter.class);
  }

  @Test
  public void itFormatsNumbers() {
    assertThat(
        jinjava.render("{{ d | format_time }}", ImmutableMap.of("d", 1668120547000L))
      )
      .isEqualTo("10:49:07 PM");
  }

  @Test
  public void itFormatsPyishDates() {
    PyishDate pyishDate = new PyishDate(1668120547000L);

    assertThat(jinjava.render("{{ d | format_time }}", ImmutableMap.of("d", pyishDate)))
      .isEqualTo("10:49:07 PM");
  }

  @Test
  public void itFormatsZonedDateTime() {
    assertThat(jinjava.render("{{ d | format_time }}", ImmutableMap.of("d", DATE_TIME)))
      .isEqualTo("10:49:07 PM");
  }

  @Test
  public void itHandlesInvalidDateInput() {
    RenderResult result = jinjava.renderForResult(
      "{{ d | format_time }}",
      ImmutableMap.of("d", "nonsense")
    );
    assertThat(result.getOutput()).isEqualTo("");
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("Input to function must be a date object, was: class java.lang.String");
  }

  @Test
  public void itUsesShortFormat() {
    assertThat(
        jinjava.render("{{ d | format_time('short') }}", ImmutableMap.of("d", DATE_TIME))
      )
      .isEqualTo("10:49 PM");
  }

  @Test
  public void itUsesLongFormat() {
    assertThat(
        jinjava.render("{{ d | format_time('long') }}", ImmutableMap.of("d", DATE_TIME))
      )
      .isEqualTo("10:49:07 PM Z");
  }

  @Test
  public void itUsesFullFormat() {
    assertThat(
        jinjava.render("{{ d | format_time('full') }}", ImmutableMap.of("d", DATE_TIME))
      )
      .isEqualTo("10:49:07 PM Z");
  }

  @Test
  public void itUsesCustomFormats() {
    assertThat(
        jinjava.render(
          "{{ d | format_time('hh:mm a') }}",
          ImmutableMap.of("d", DATE_TIME)
        )
      )
      .isEqualTo("10:49 PM");
  }

  @Test
  public void itHandlesInvalidFormats() {
    RenderResult result = jinjava.renderForResult(
      "{{ d | format_time('fake pattern') }}",
      ImmutableMap.of("d", DATE_TIME)
    );
    assertThat(result.getOutput()).isEqualTo("");
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("Invalid date format")
      .contains("Unknown pattern letter: f");
  }

  @Test
  public void itUsesGivenTimeZone() {
    assertThat(
        jinjava.render(
          "{{ d | format_time('long', 'America/New_York') }}",
          ImmutableMap.of("d", DATE_TIME)
        )
      )
      .isEqualTo("5:49:07 PM EST");
  }

  @Test
  public void itHandlesInvalidTimeZones() {
    RenderResult result = jinjava.renderForResult(
      "{{ d | format_time('long', 'not a real time zone') }}",
      ImmutableMap.of("d", DATE_TIME)
    );
    assertThat(result.getOutput()).isEqualTo("");
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("Invalid time zone: not a real time zone");
  }

  @Test
  public void itHandlesEmptyTimeZones() {
    RenderResult result = jinjava.renderForResult(
      "{{ d | format_time('long', '') }}",
      ImmutableMap.of("d", DATE_TIME)
    );
    assertThat(result.getOutput()).isEqualTo("");
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage()).contains("Invalid time zone: ");
  }

  @Test
  public void itUsesGivenLocale() {
    assertThat(
        jinjava.render(
          "{{ d | format_time('medium', 'America/New_York', 'de-DE') }}",
          ImmutableMap.of("d", DATE_TIME)
        )
      )
      .isEqualTo("17:49:07");
  }

  @Test
  public void itHandlesInvalidLocales() {
    RenderResult result = jinjava.renderForResult(
      "{{ d | format_time('medium', 'America/New_York', 'not a real locale') }}",
      ImmutableMap.of("d", DATE_TIME)
    );
    assertThat(result.getOutput()).isEqualTo("");
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("Invalid locale: not a real locale");
  }

  @Test
  public void itHandlesEmptyLocales() {
    RenderResult result = jinjava.renderForResult(
      "{{ d | format_time('medium', 'America/New_York', '') }}",
      ImmutableMap.of("d", DATE_TIME)
    );
    assertThat(result.getOutput()).isEqualTo("");
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage()).contains("Invalid locale: ");
  }
}
