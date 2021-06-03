package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.InvalidDateFormatException;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class DateTimeFormatFilterTest extends BaseInterpretingTest {
  private static final JinjavaConfig JAPANESE_LOCALE_CONFIG = new JinjavaConfig(
    StandardCharsets.UTF_8,
    new Locale("ja", "JA"),
    ZoneOffset.ofHours(9),
    10
  );

  private DateTimeFormatFilter filter;

  private ZonedDateTime d;

  @Before
  public void setup() {
    Locale.setDefault(Locale.ENGLISH);
    filter = new DateTimeFormatFilter();
    d = ZonedDateTime.parse("2013-11-06T14:22:00.000+00:00[UTC]");
  }

  @Test
  public void itUsesTodayIfNoDateProvided() throws Exception {
    assertThat(filter.filter(null, interpreter))
      .isEqualTo(StrftimeFormatter.format(ZonedDateTime.now(ZoneOffset.UTC)));
  }

  @Test
  public void itSupportsLongAsInput() throws Exception {
    assertThat(filter.filter(d, interpreter)).isEqualTo(StrftimeFormatter.format(d));
  }

  @Test
  public void itUsesDefaultFormatStringIfNoneSpecified() throws Exception {
    assertThat(filter.filter(d, interpreter)).isEqualTo("14:22 / 06-11-2013");
  }

  @Test
  public void itUsesSpecifiedFormatString() throws Exception {
    assertThat(filter.filter(d, interpreter, "%B %d, %Y, at %I:%M %p"))
      .isEqualTo("November 06, 2013, at 02:22 PM");
  }

  @Test
  public void itHandlesVarsAndLiterals() throws Exception {
    interpreter.getContext().put("d", d);
    interpreter.getContext().put("foo", "%Y-%m");

    assertThat(interpreter.renderFlat("{{ d|datetimeformat(foo) }}"))
      .isEqualTo("2013-11");
    assertThat(interpreter.renderFlat("{{ d|datetimeformat(\"%Y-%m-%d\") }}"))
      .isEqualTo("2013-11-06");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itSupportsTimezones() throws Exception {
    assertThat(filter.filter(1539277785000L, interpreter, "%B %d, %Y, at %I:%M %p"))
      .isEqualTo("October 11, 2018, at 05:09 PM");
    assertThat(
        filter.filter(
          1539277785000L,
          interpreter,
          "%B %d, %Y, at %I:%M %p",
          "America/New_York"
        )
      )
      .isEqualTo("October 11, 2018, at 01:09 PM");
    assertThat(
        filter.filter(1539277785000L, interpreter, "%B %d, %Y, at %I:%M %p", "UTC+8")
      )
      .isEqualTo("October 12, 2018, at 01:09 AM");
  }

  @Test(expected = InvalidDateFormatException.class)
  public void itThrowsExceptionOnInvalidTimezone() throws Exception {
    filter.filter(
      1539277785000L,
      interpreter,
      "%B %d, %Y, at %I:%M %p",
      "Not a timezone"
    );
  }

  @Test(expected = InvalidDateFormatException.class)
  public void itThrowsExceptionOnInvalidDateformat() throws Exception {
    filter.filter(1539277785000L, interpreter, "Not a format");
  }

  @Test
  public void itConvertsDatetimesByLocales() {
    interpreter.getContext().put("d", d);

    assertThat(interpreter.renderFlat("{{ d|datetimeformat('%A, %e %B', 'UTC', 'sv') }}"))
      .isEqualTo("onsdag, 6 november");
  }

  @Test
  public void itDefaultsToEnglishForBadLocaleValues() {
    interpreter.getContext().put("d", d);

    assertThat(
        interpreter.renderFlat(
          "{{ d|datetimeformat('%A, %e %B', 'UTC', 'not_a_locale') }}"
        )
      )
      .isEqualTo(Functions.dateTimeFormat(d, "%A, %e %B", "UTC", "America/Los_Angeles"));
  }

  @Test
  public void itUsesConfigZoneAsDefault() {
    JinjavaInterpreter japaneseInterpreter = new Jinjava(JAPANESE_LOCALE_CONFIG)
    .newInterpreter();

    try {
      JinjavaInterpreter.pushCurrent(japaneseInterpreter);

      // before UTC midnight on 6/10 (or 6/11 in Japanese timezone)
      japaneseInterpreter.getContext().put("d", 1623337200000L);

      assertThat(
          japaneseInterpreter.renderFlat(
            "{{ d|datetimeformat('%m') }}/{{ d|datetimeformat('%e') }}"
          )
        )
        .isEqualTo("06/11");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }
}
