package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class TypeFunctionTest {

  private JinjavaInterpreter interpreter;

  @Before
  public void setup() throws NoSuchMethodException {

    interpreter = new Jinjava().newInterpreter();
  }

  @Test
  public void testString() {
    assertThat(TypeFunction.type(" foo  ")).isEqualTo("str");
  }

  @Test
  public void testInteger() {
    assertThat(TypeFunction.type(123)).isEqualTo("int");
  }

  @Test
  public void testLong() {
    assertThat(TypeFunction.type(123L)).isEqualTo("long");
  }

  @Test
  public void testDouble() {
    assertThat(TypeFunction.type(123.3345d)).isEqualTo("float");
  }

  @Test
  public void testDate() {
    assertThat(TypeFunction.type(ZonedDateTime.parse("2013-11-06T14:22:00.000+00:00[UTC]"))).isEqualTo("datetime");
  }

  @Test
  public void testList() {
    assertThat(TypeFunction.type(new ArrayList<>())).isEqualTo("list");
  }

  @Test
  public void testDict() throws Exception {
    assertThat(TypeFunction.type(new HashMap<>())).isEqualTo("dict");
  }

  @Test
  public void testBool() {
    assertThat(TypeFunction.type(Boolean.FALSE)).isEqualTo("bool");
  }
}
