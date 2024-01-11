package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;

public class LogFilterTest extends BaseInterpretingTest {

  LogFilter filter;

  @Before
  public void setup() {
    filter = new LogFilter();
  }

  @Test
  public void itCalculatesLogBase2() {
    assertThat(filter.filter(65536, interpreter, "2")).isEqualTo(16D);
  }

  @Test
  public void itCalculatesLogBase10() {
    assertThat(filter.filter(100, interpreter, "10")).isEqualTo(2D);
  }

  @Test
  public void itCalculatesLogBaseN() {
    assertThat(filter.filter(9765625d, interpreter, "45.123"))
      .isEqualTo(4.224920597763891);
  }

  @Test
  public void itCalculatesBigDecimalLogBaseN() {
    BigDecimal result = (BigDecimal) filter.filter(
      new BigDecimal(9765625d),
      interpreter,
      "45.123"
    );
    assertThat(result.doubleValue()).isEqualTo(4.224920597763891);
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
