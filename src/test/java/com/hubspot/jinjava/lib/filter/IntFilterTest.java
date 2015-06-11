package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class IntFilterTest {

  IntFilter filter;
  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new IntFilter();
  }

  @Test
  public void itReturnsSameWhenVarIsNumber() {
    Integer var = Integer.valueOf(123);
    assertThat(filter.filter(var, interpreter)).isSameAs(var);
  }

  @Test
  public void itReturnsDefaultWhenVarIsNull() {
    assertThat(filter.filter(null, interpreter)).isEqualTo(0);
    assertThat(filter.filter(null, interpreter, "123")).isEqualTo(123);
  }

  @Test
  public void itIgnoresGivenDefaultIfNaN() {
    assertThat(filter.filter(null, interpreter, "foo")).isEqualTo(0);
  }

  @Test
  public void itReturnsVarAsInt() {
    assertThat(filter.filter("123", interpreter)).isEqualTo(123);
  }

  @Test
  public void itReturnsDefaultWhenUnableToParseVar() {
    assertThat(filter.filter("foo", interpreter)).isEqualTo(0);
  }

}
