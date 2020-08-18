package com.hubspot.jinjava.el.ext;

import static org.junit.Assert.assertEquals;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class DivTest {
  private Jinjava jinja;

  @Before
  public void setUp() {
    jinja = new Jinjava();
  }

  @Test
  public void itDividesWithNonZeroLongDivisor() {
    assertEquals(jinja.render("{% set x = 10 / 2%}{{x}}", new HashMap<>()), "5.0");
  }

  @Test
  public void itDividesWithNonZeroDoubleDivisor() {
    assertEquals(jinja.render("{% set x = 10.0 / 2.0%}{{x}}", new HashMap<>()), "5.0");
  }

  @Test
  public void itErrorsOutWithZeroDivisor() {
    RenderResult result = jinja.renderForResult(
      "{% set x = 10.0 / 0.0%}{{x}}",
      new HashMap<>()
    );

    assertEquals(result.getErrors().size(), 1);
    TemplateError error = result.getErrors().get(0);
    assertEquals(error.getSeverity(), ErrorType.FATAL);
    assertEquals(error.getReason(), ErrorReason.INVALID_ARGUMENT);
    assertEquals(error.getMessage().contains("Divisor may not be zero"), true);
  }
}
