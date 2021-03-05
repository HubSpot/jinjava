package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class StringToDateFunctionTest {

  @Test
  public void itConvertsStringToDate() {
    String dateString = "3/4/21";
    String format = "M/d/yy";
    PyishDate expected = new PyishDate(
      LocalDate.of(2021, 3, 4).atTime(0, 0).toInstant(ZoneOffset.UTC)
    );
    assertThat(Functions.stringToDate(dateString, format)).isEqualTo(expected);
  }

  @Test
  public void itFailsOnInvalidFormat() {
    String dateString = "3/4/21";
    String format = "blah blah";

    assertThatThrownBy(() -> Functions.stringToDate(dateString, format))
      .isInstanceOf(InterpretException.class);
  }

  @Test
  public void itFailsOnTimeFormatMismatch() {
    String dateString = "3/4/21";
    String format = "M/d/yyyy";

    assertThatThrownBy(() -> Functions.stringToDate(dateString, format))
      .isInstanceOf(InterpretException.class);
  }

  @Test
  public void itFailsOnNullDatetimeFormat() {
    String dateString = "3/4/21";
    String format = null;

    assertThatThrownBy(() -> Functions.stringToDate(dateString, format))
      .isInstanceOf(InterpretException.class);
  }
}
