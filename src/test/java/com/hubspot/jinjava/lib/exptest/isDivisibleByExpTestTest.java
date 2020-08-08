package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import java.util.Date;
import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
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
  public void itRequiresDivisor() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "null"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itPassesWhenDividendIsDivisibleByDivisor() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "5"), new HashMap<>()),
      "true"
    );
  }

  @Test
  public void itFailsWhenDividendIsNotMultipleOfDivisor() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "3"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itRequiresNonZeroDivisor() {
    RenderResult result = jinjava.renderForResult(
      String.format(DIVISIBLE_BY_TEMPLATE, "10", "0"),
      new HashMap<>()
    );
    assertEquals(result.getErrors().size(), 1);
    TemplateError error = result.getErrors().get(0);
    assertEquals(error.getSeverity(), ErrorType.FATAL);
    assertEquals(error.getReason(), ErrorReason.INVALID_ARGUMENT);
    assertEquals(
      error.getMessage().contains("1st argument with value 0 must be non-zero"),
      true
    );
  }
}
