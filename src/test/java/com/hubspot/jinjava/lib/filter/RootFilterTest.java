package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class RootFilterTest {

  JinjavaInterpreter interpreter;
  RootFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new RootFilter();
  }

  @Test
  public void itCalculatesSquareRoot() {
    assertThat(filter.filter(100, interpreter)).isEqualTo(10D);
    assertThat(filter.filter(22500, interpreter)).isEqualTo(150D);
  }


  @Test
  public void itCalculatesCubeRoot() {
    assertThat(filter.filter(125, interpreter, "3")).isEqualTo(5d);
  }

  @Test
  public void itCalculatesNthRoot() {
    assertThat(filter.filter(9765625d, interpreter, "5.0")).isEqualTo(25d);
  }

  @Test
  public void itCalculatesNthRootOfBigDecimal() {
    BigDecimal result = ((BigDecimal) filter.filter(new BigDecimal(9765625d), interpreter, "5.0"));
    assertThat(result.doubleValue()).isEqualTo(25d);
  }

  @Test(expected = InvalidInputException.class)
  public void itErrorsOnNegativeInput() {
    filter.filter(-10d, interpreter, "5.0");
  }

  @Test(expected = InvalidInputException.class)
  public void itErrorsOnStringInput() {
    filter.filter("not a number", interpreter, "5.0");
  }

  @Test(expected = InvalidArgumentException.class)
  public void itErrorsOnNegativeArgument() {
    filter.filter(10d, interpreter, "-5.0");
  }

  @Test(expected = InvalidArgumentException.class)
  public void itErrorsOnStringArgument() {
    filter.filter(10d, interpreter, "not a number");
  }
}
