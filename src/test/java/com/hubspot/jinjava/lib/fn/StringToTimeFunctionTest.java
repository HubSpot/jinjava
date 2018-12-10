package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.objects.date.PyishDate;

public class StringToTimeFunctionTest {

  @Test
  public void itConvertsStringToTime() {

    String datetime = "2018-07-14T14:31:30+0530";
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";
    PyishDate expected = new PyishDate(ZonedDateTime.of(2018, 7, 14, 14, 31, 30, 0, ZoneOffset.ofHoursMinutes(5, 30)));
    assertThat(Functions.stringToTime(datetime, format)).isEqualTo(expected);
  }

  @Test(expected = InterpretException.class)
  public void itFailsOnInvalidFormat() {

    String datetime = "2018-07-14T14:31:30+0530";
    String format = "not a time format";
    PyishDate expected = new PyishDate(ZonedDateTime.of(2018, 7, 14, 14, 31, 30, 0, ZoneOffset.ofHoursMinutes(5, 30)));
    assertThat(Functions.stringToTime(datetime, format)).isEqualTo(expected);
  }

  @Test(expected = InterpretException.class)
  public void itFailsOnTimeFormatMismatch() {

    String datetime = "Saturday, Jul 14, 2018 14:31:06 PM";
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";
    PyishDate expected = new PyishDate(ZonedDateTime.of(2018, 7, 14, 14, 31, 30, 0, ZoneOffset.ofHoursMinutes(5, 30)));
    assertThat(Functions.stringToTime(datetime, format)).isEqualTo(expected);
  }

  public void itReturnsNullOnNullInput() {

    String datetime = null;
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";
    assertThat(Functions.stringToTime(datetime, format)).isEqualTo(null);
  }

  @Test(expected = InterpretException.class)
  public void itFailsOnNullDatetimeFormat() {

    String datetime = "2018-07-14T14:31:30+0530";
    String format = null;
    assertThat(Functions.stringToTime(datetime, format)).isEqualTo(null);
  }
}
