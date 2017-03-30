package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class TypeOfFilterTest {

  JinjavaInterpreter interpreter;
  TypeOfFilter filter;
  private Jinjava jinjava;
  private Map<String, Object> context;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    context = new HashMap<>();
    interpreter = jinjava.newInterpreter();
    filter = new TypeOfFilter();
  }

  @Test
  public void testString() {
    assertThat(filter.filter(" foo  ", interpreter)).isEqualTo("string");
    assertThat(interpreter.renderFlat("{{ \"123\"|typeof }}")).isEqualTo("string");
  }

  @Test
  public void testInteger() {
    assertThat(filter.filter(123, interpreter)).isEqualTo("number");
  }

  @Test
  public void testDouble() {
    assertThat(filter.filter(123.3345d, interpreter)).isEqualTo("number");
  }

  @Test
  public void testDate() {
    assertThat(filter.filter(ZonedDateTime.parse("2013-11-06T14:22:00.000+00:00[UTC]"), interpreter)).isEqualTo("datetime");
  }

  @Test
  public void testList() {
    assertThat(filter.filter(new ArrayList<>(), interpreter)).isEqualTo("list");
    assertThat(interpreter.renderFlat("{{ [1,2,3]|typeof }}")).isEqualTo("list");
  }

  @Test
  public void testMap() throws Exception {
    assertThat(interpreter.renderFlat("{{ [1,2,3]|typeof }}")).isEqualTo("dict");
  }

  @Test
  public void testNumber() throws Exception {
    assertThat(interpreter.renderFlat("{{ 123|typeof }}")).isEqualTo("number");
    assertThat(interpreter.renderFlat("{{ 3446.5|typeof }}")).isEqualTo("number");
    assertThat(interpreter.renderFlat("{{ (123/3446.5)|typeof }}")).isEqualTo("number");
  }

  @Test
  public void testBool() {
    assertThat(interpreter.renderFlat("{{ 1|bool|typeof }}")).isEqualTo("boolean");
    assertThat(interpreter.renderFlat("{{ 0|bool|typeof }}")).isEqualTo("boolean");
  }

  @Test
  public void testDict() throws Exception {
    assertThat(interpreter.renderFlat("{{ 1|bool|typeof }}")).isEqualTo("boolean");


  }

  // chararray?
}
