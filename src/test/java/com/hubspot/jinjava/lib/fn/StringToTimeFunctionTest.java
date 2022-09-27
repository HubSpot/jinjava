package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Test;

public class StringToTimeFunctionTest {

  @Test
  public void itConvertsStringToTime() {
    String datetime = "2018-07-14T14:31:30+0530";
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";
    PyishDate expected = new PyishDate(
      ZonedDateTime.of(2018, 7, 14, 14, 31, 30, 0, ZoneOffset.ofHoursMinutes(5, 30))
    );
    assertThat(Functions.stringToTime(datetime, format)).isEqualTo(expected);
  }

  @Test
  public void itFailsOnInvalidFormat() {
    String datetime = "2018-07-14T14:31:30+0530";
    String format = "not a time format";

    assertThatExceptionOfType(InterpretException.class)
      .isThrownBy(() -> Functions.stringToTime(datetime, format))
      .withMessageContaining("requires valid datetime format");
  }

  @Test
  public void itFailsOnTimeFormatMismatch() {
    String datetime = "Saturday, Jul 14, 2018 14:31:06 PM";
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";

    assertThatExceptionOfType(InterpretException.class)
      .isThrownBy(() -> Functions.stringToTime(datetime, format))
      .withMessageContaining("could not match datetime input");
  }

  public void itReturnsNullOnNullInput() {
    String datetime = null;
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";
    assertThat(Functions.stringToTime(datetime, format)).isEqualTo(null);
  }

  @Test
  public void itFailsOnNullDatetimeFormat() {
    String datetime = "2018-07-14T14:31:30+0530";
    String format = null;

    assertThatExceptionOfType(InterpretException.class)
      .isThrownBy(() -> Functions.stringToTime(datetime, format))
      .withMessageContaining("requires non-null datetime format");
  }
}
