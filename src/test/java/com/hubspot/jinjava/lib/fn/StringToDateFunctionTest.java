package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.objects.date.PyishDate;
import org.junit.Test;

public class StringToDateFunctionTest {

  @Test
  public void itConvertsStringToDate() {
    String dateString = "3/3/21";
    String dateFormat = "M/d/yy";

    PyishDate date = Functions.stringToDate(dateString, dateFormat);

    assertThat(date.getDay()).isEqualTo(3);
    assertThat(date.getMonth()).isEqualTo(3);
    assertThat(date.getYear()).isEqualTo(2021);
  }
}
