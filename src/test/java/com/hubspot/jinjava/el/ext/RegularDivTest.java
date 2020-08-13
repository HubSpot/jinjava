package com.hubspot.jinjava.el.ext;

import static org.junit.Assert.assertEquals;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class RegularDivTest {
  private Jinjava jinja;

  @Before
  public void setUp() {
    jinja = new Jinjava();
  }

  @Test
  public void okOnLongDivisionWithNonZeroDivisor() {
    assertEquals(jinja.render("{% set x = 10 / 2%}{{x}}", new HashMap<>()), "5.0");
  }

  @Test
  public void okOnDoubleDivisionWithNonZeroDivisor() {
    assertEquals(jinja.render("{% set x = 10.0 / 2.0%}{{x}}", new HashMap<>()), "5.0");
  }
}
