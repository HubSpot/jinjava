package com.hubspot.jinjava.objects.date;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class StrftimeFormatterTest {

  ZonedDateTime d;

  @Before
  public void setup() {
    Locale.setDefault(Locale.ENGLISH);
    d = ZonedDateTime.parse("2013-11-06T14:22:00.000+00:00");
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
  public void testWithNoPcts() {
    assertThat(StrftimeFormatter.format(d, "MMMM yyyy")).isEqualTo("November 2013");
  }

  @Test
  public void testDateTime() {
    assertThat(StrftimeFormatter.format(d, "%c")).isEqualTo("Wed Nov 06 14:22:00 2013");
  }

  @Test
  public void testDate() {
    assertThat(StrftimeFormatter.format(d, "%x")).isEqualTo("11/06/13");
  }

  @Test
  public void testDayOfWeekNumber() {
    assertThat(StrftimeFormatter.format(d, "%w")).isEqualTo("4");
  }

  @Test
  public void testTime() {
    assertThat(StrftimeFormatter.format(d, "%X")).isEqualTo("14:22:00");
  }

  @Test
  public void testWithLL() {
    assertThat(StrftimeFormatter.format(d, "yyyy/LL/dd HH:mm")).isEqualTo("2013/11/06 14:22");
  }

  @Test
  public void testPaddedMinFmt() {
    ZonedDateTime dateTime = ZonedDateTime.parse("2013-11-06T04:02:00.000+00:00");

    assertThat(StrftimeFormatter.format(dateTime, "%I")).isEqualTo("04");
    assertThat(StrftimeFormatter.format(dateTime, "%l")).isEqualTo("4");
  }

  @Test
  public void testFinnishMonths() {
    assertThat(StrftimeFormatter.formatter("long").withLocale(Locale.forLanguageTag("fi")).format(d))
        .startsWith("6. marraskuuta 2013 klo 14.22.00");
  }

}
