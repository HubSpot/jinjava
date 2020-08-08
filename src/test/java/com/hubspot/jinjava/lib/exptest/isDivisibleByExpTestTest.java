package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class isDivisibleByExpTestTest {
  private static final String DIVISIBLE_BY_TEMPLATE = "{{ %s is divisibleby %s }}";

  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itRequiresDividend() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "null", "3"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itRequiresNumericalDividend() {
    assertEquals(
      jinjava.render(
        String.format(DIVISIBLE_BY_TEMPLATE, "thirty", "3"),
        new HashMap<>()
      ),
      "false"
    );
  }

  @Test
  public void itRequiresDivisor() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "null"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itRequiresNumericalDivisor() {
    // Question for PR: It seems that InvalidArgumentExceptions are caught before my middleware. How to know it failed for the right reason?
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "five"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itRequiresDividendIsMultipleOfDivisor() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "5"), new HashMap<>()),
      "true"
    );

    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "3"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itRequiresNonZeroDivisor() {
    assertThatThrownBy(
        () ->
          jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "0"), new HashMap<>())
      )
      .hasMessageContaining("1st argument with value 0 must be non-zero");
  }
}
