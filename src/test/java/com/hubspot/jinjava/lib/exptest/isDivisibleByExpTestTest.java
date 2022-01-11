package com.hubspot.jinjava.lib.exptest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import java.util.HashMap;
import org.junit.Test;

public class isDivisibleByExpTestTest extends BaseJinjavaTest {
  private static final String DIVISIBLE_BY_TEMPLATE = "{{ %s is divisibleby %s }}";

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
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "'foo'", "3"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itReturnsFalseForFractionalDividend() {
    assertEquals(
      jinjava.render(
        String.format(DIVISIBLE_BY_TEMPLATE, "10.00001", "5"),
        new HashMap<>()
      ),
      "false"
    );
  }

  @Test(expected = FatalTemplateErrorsException.class)
  public void itRequiresDivisor() {
    jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "null"), new HashMap<>());
    fail("This line shouldn't be reached!");
  }

  @Test
  public void itRequiresNumericalDivisor() {
    RenderResult result = jinjava.renderForResult(
      String.format(DIVISIBLE_BY_TEMPLATE, "10", "'foo'"),
      new HashMap<>()
    );
    assertOnInvalidArgument(result, "must be a number");
  }

  @Test
  public void itRequiresNonZeroDivisor() {
    RenderResult result = jinjava.renderForResult(
      String.format(DIVISIBLE_BY_TEMPLATE, "10", "0"),
      new HashMap<>()
    );
    assertOnInvalidArgument(result, "1st argument with value 0 must be non-zero");
  }

  @Test
  public void itReturnsTrueWhenDividendIsDivisibleByDivisor() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "5"), new HashMap<>()),
      "true"
    );
  }

  @Test
  public void itReturnsTrueForDecimalDividendWithNoFractionalPart() {
    assertEquals(
      jinjava.render(
        String.format(DIVISIBLE_BY_TEMPLATE, "10.00000", "5"),
        new HashMap<>()
      ),
      "true"
    );
  }

  @Test
  public void itReturnsFalseWhenDividendIsNotMultipleOfDivisor() {
    assertEquals(
      jinjava.render(String.format(DIVISIBLE_BY_TEMPLATE, "10", "3"), new HashMap<>()),
      "false"
    );
  }

  @Test
  public void itReturnsFalseForFractionalDivisor() {
    RenderResult result = jinjava.renderForResult(
      String.format(DIVISIBLE_BY_TEMPLATE, "10", "5.00001"),
      new HashMap<>()
    );
    assertOnInvalidArgument(result, "must be non-zero");
  }

  @Test
  public void itReturnsTrueForDecimalDivisorWithNoFractionalPart() {
    assertEquals(
      jinjava.render(
        String.format(DIVISIBLE_BY_TEMPLATE, "10", "5.00000"),
        new HashMap<>()
      ),
      "true"
    );
  }

  private void assertOnInvalidArgument(
    RenderResult result,
    String expectedMessageIfInvalid
  ) {
    assertEquals(result.getErrors().size(), 1);
    TemplateError error = result.getErrors().get(0);
    assertEquals(error.getSeverity(), ErrorType.FATAL);
    assertEquals(error.getReason(), ErrorReason.INVALID_ARGUMENT);
    assertEquals(error.getMessage().contains(expectedMessageIfInvalid), true);
  }
}
