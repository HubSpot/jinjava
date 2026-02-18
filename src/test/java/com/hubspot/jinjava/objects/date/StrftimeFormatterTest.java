package com.hubspot.jinjava.objects.date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class StrftimeFormatterTest {

  ZonedDateTime d;

  @Before
  public void setup() {
    Locale.setDefault(Locale.ENGLISH);
    d =
      ZonedDateTime
        .parse("2013-11-06T14:22:00.123+00:00")
        .withZoneSameLocal(ZoneId.of("UTC"));
  }

  @Test
  public void testUtf8Chars() {
    assertThat(StrftimeFormatter.format(d, "%Y年%m月%d日")).isEqualTo("2013年11月06日");
  }

  @Test
  public void testDefaultFormat() {
    assertThat(StrftimeFormatter.format(d)).isEqualTo("14:22 / 06-11-2013");
  }

  @Test
  public void testCommentsFormat() {
    assertThat(StrftimeFormatter.format(d, "%B %d, %Y, at %I:%M %p"))
      .isEqualTo("November 06, 2013, at 02:22 PM");
  }

  @Test
  public void testFormatWithDash() {
    assertThat(StrftimeFormatter.format(d, "%B %-d, %Y")).isEqualTo("November 6, 2013");
  }

  @Test
  public void testFormatWithTrailingPercent() {
    assertThat(StrftimeFormatter.format(d, "%B %-d, %")).isEqualTo("November 6, %");
  }

  @Test
  public void testWithNoPcts() {
    assertThat(StrftimeFormatter.format(d, "MMMM yyyy")).isEqualTo("November 2013");
  }

  @Test
  public void testDateTime() {
    assertThat(StrftimeFormatter.format(d, "%c"))
      .isIn("Nov 6, 2013, 2:22:00 PM", "Nov 6, 2013, 2:22:00 PM");
  }

  @Test
  public void testDate() {
    assertThat(StrftimeFormatter.format(d, "%x")).isEqualTo("11/6/13");
  }

  @Test
  public void testDayOfWeekNumber() {
    assertThat(StrftimeFormatter.format(d, "%w")).isEqualTo("4");
  }

  @Test
  public void testTime() {
    assertThat(StrftimeFormatter.format(d, "%X")).isIn("2:22:00 PM", "2:22:00 PM");
  }

  @Test
  public void testMicrosecs() {
    assertThat(StrftimeFormatter.format(d, "%X %f"))
      .isIn("2:22:00 PM 123000", "2:22:00 PM 123000");
  }

  @Test
  public void testWithLL() {
    assertThat(StrftimeFormatter.format(d, "yyyy/LL/dd HH:mm"))
      .isEqualTo("2013/11/06 14:22");
  }

  @Test
  public void testPaddedMinFmt() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2013-11-06T04:02:00.000+00:00");

    assertThat(StrftimeFormatter.format(dateTime, "%I")).isEqualTo("04");
    assertThat(StrftimeFormatter.format(dateTime, "%l")).isEqualTo("4");
  }

  @Test
  public void testFinnishMonths() {
    assertThat(StrftimeFormatter.format(d, "long", Locale.forLanguageTag("fi")))
      .isIn("6. marraskuuta 2013 klo 14.22.00 UTC", "6. marraskuuta 2013 14.22.00 UTC");
  }

  @Test
  public void testZoneOutput() {
    assertThat(StrftimeFormatter.format(d, "%z")).isEqualTo("+0000");
    assertThat(StrftimeFormatter.format(d, "%Z")).isEqualTo("UTC");

    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
      d.toInstant(),
      ZoneId.of("America/New_York")
    );
    assertThat(StrftimeFormatter.format(zonedDateTime, "%Z")).isEqualTo("EST");
  }

  @Test
  public void itConvertsNominativeFormats() {
    ZonedDateTime zonedDateTime = ZonedDateTime.parse("2019-06-06T14:22:00.000+00:00");

    assertThat(
      StrftimeFormatter.format(zonedDateTime, "%OB", Locale.forLanguageTag("ru"))
    )
      .isIn("Июнь", "июнь");
  }

  @Test
  public void itThrowsOnInvalidFormats() {
    assertThatExceptionOfType(InvalidDateFormatException.class)
      .isThrownBy(() -> StrftimeFormatter.format(d, "%d.%é.%Y"))
      .withMessage("Invalid date format '%d.%é.%Y'");

    assertThatExceptionOfType(InvalidDateFormatException.class)
      .isThrownBy(() -> StrftimeFormatter.format(d, "%d.%ğ.%Y"))
      .withMessage("Invalid date format '%d.%ğ.%Y'");
  }

  @Test
  public void itOutputsLiteralPercents() {
    assertThat(StrftimeFormatter.format(d, "hi %% there")).isEqualTo("hi % there");
    assertThat(StrftimeFormatter.format(d, "%%")).isEqualTo("%");
  }

  @Test
  public void itIgnoresFinalStandalonePercent() {
    assertThat(StrftimeFormatter.format(d, "%")).isEqualTo("%");
  }

  @Test
  public void itAllowsLiteralCharacters() {
    assertThat(StrftimeFormatter.format(d, "1: day %d month %B"))
      .isEqualTo("1: day 06 month November");
  }

  @Test
  public void itUsesInterpreterLocaleAsDefault() {
    try {
      Jinjava jinjava = new Jinjava(
        BaseJinjavaTest.newConfigBuilder().withLocale(Locale.FRENCH).build()
      );
      JinjavaInterpreter.pushCurrent(jinjava.newInterpreter());
      assertThat(StrftimeFormatter.format(d, "%B %-d, %Y")).isEqualTo("novembre 6, 2013");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }
}
