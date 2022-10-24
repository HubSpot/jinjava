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
    PyishDate expected = new PyishDate(
      ZonedDateTime.of(2018, 7, 14, 14, 31, 30, 0, ZoneOffset.ofHoursMinutes(5, 30))
    );
    assertThat(
        Functions.stringToTime("2018-07-14T14:31:30+0530", "yyyy-MM-dd'T'HH:mm:ssZ")
      )
      .isEqualTo(expected);
  }

  @Test
  public void itFailsOnInvalidFormat() {
    assertThatExceptionOfType(InterpretException.class)
      .isThrownBy(
        () -> Functions.stringToTime("2018-07-14T14:31:30+0530", "not a time format")
      )
      .withMessageContaining("requires valid datetime format");
  }

  @Test
  public void itFailsOnTimeFormatMismatch() {
    assertThatExceptionOfType(InterpretException.class)
      .isThrownBy(
        () ->
          Functions.stringToTime(
            "Saturday, Jul 14, 2018 14:31:06 PM",
            "yyyy-MM-dd'T'HH:mm:ssZ"
          )
      )
      .withMessageContaining("could not match datetime input");
  }

  @Test
  public void itReturnsNullOnNullInput() {
    assertThat(Functions.stringToTime(null, "yyyy-MM-dd'T'HH:mm:ssZ")).isEqualTo(null);
  }

  @Test
  public void itFailsOnNullDatetimeFormat() {
    assertThatExceptionOfType(InterpretException.class)
      .isThrownBy(() -> Functions.stringToTime("2018-07-14T14:31:30+0530", null))
      .withMessageContaining("requires non-null datetime format");
  }
}
