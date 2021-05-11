package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TitleFilterTest {

  @Test
  public void itTitleCasesNormalString() {
    assertThat(new TitleFilter().filter("this is string", null))
      .isEqualTo("This Is String");
  }

  @Test
  public void itDoesNotChangeAlreadyTitleCasedString() {
    assertThat(new TitleFilter().filter("This Is String", null))
      .isEqualTo("This Is String");
  }

  @Test
  public void itLowercasesOtherUppercasedCharactersInString() {
    assertThat(new TitleFilter().filter("this is sTRING", null))
      .isEqualTo("This Is String");
  }
}
