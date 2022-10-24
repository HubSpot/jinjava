package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.InvalidDateFormatException;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class DateTimeFormatFilterTest extends BaseInterpretingTest {
  DateTimeFormatFilter filter;

  ZonedDateTime d;

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

  @Test(expected = InvalidArgumentException.class)
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
  public void itDefaultsToUtcForNullTimezone() {
    interpreter.getContext().put("d", d);

    assertThat(
        interpreter.renderFlat(
          "{{ d|datetimeformat('%A, %e %B, %I:%M %p', null, 'sv') }}"
        )
      )
      .isEqualTo("onsdag, 6 november, 02:22 em");
  }

  @Test
  public void itHandlesInvalidDateFormats() {
    RenderResult result = jinjava.renderForResult(
      "{{ d | datetimeformat('%é') }}",
      ImmutableMap.of("d", d)
    );

    assertThat(result.getOutput()).isEqualTo("");
    assertThat(result.getErrors()).hasSize(1);

    TemplateError error = result.getErrors().get(0);
    assertThat(error.getSeverity()).isEqualTo(ErrorType.FATAL);
    assertThat(error.getMessage()).contains("Invalid date format: [%é]");

    /*
    datetimeformat outputs the string "null" for unrecognized format codes,
    which DateTimeFormatter then tries to interpret as a pattern. 'n' and 'u'
    are valid pattern letters, but 'l' is not, hence the following error message.
    */
    assertThat(error.getMessage()).contains("Unknown pattern letter: l");
  }
}
