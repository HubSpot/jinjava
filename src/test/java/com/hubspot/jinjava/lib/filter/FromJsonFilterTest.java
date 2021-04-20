package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.InvalidInputException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FromJsonFilterTest extends BaseInterpretingTest {
  private FromJsonFilter filter;

  @Before
  public void setup() {
    filter = new FromJsonFilter();
  }

  @Test(expected = InvalidInputException.class)
  public void itFailsWhenStringIsNotJson() {
    String json = "blah";

    filter.filter(json, interpreter);
  }

  @Test(expected = InvalidInputException.class)
  public void itFailsWhenParameterIsNotString() {
    Integer json = 456;

    filter.filter(json, interpreter);
  }

  @Test(expected = InvalidInputException.class)
  public void itFailsWhenJsonIsInvalid() {
    String json = "{[ }]";

    filter.filter(json, interpreter);
  }

  @Test
  public void itRendersTrivialJsonObject() {
    String trivialJsonObject = "{\"a\":100,\"b\":200}";

    Map<String, Object> vars = ImmutableMap.of("test", trivialJsonObject);
    String template = "{% set obj = test | fromjson %}{{ obj.a }} {{ obj.b }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("100 200");
  }

  @Test
  public void itRendersTrivialJsonArray() {
    String trivialJsonArray = "[\"one\",\"two\",\"three\"]";

    Map<String, Object> vars = ImmutableMap.of("test", trivialJsonArray);
    String template =
      "{% set obj = test | fromjson %}{{ obj[0] }} {{ obj[1] }} {{ obj[2] }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("one two three");
  }

  @Test
  public void itRendersNestedObjectJson() {
    String nestedObject = "{\"first\": 1,\"nested\":{\"second\":\"string\",\"third\":4}}";

    Map<String, Object> vars = ImmutableMap.of("test", nestedObject);
    String template =
      "{% set obj = test | fromjson %}{{ obj.first }} {{ obj.nested.second }} {{ obj.nested.third }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("1 string 4");
  }

  @Test
  public void itRendersNestedJsonWithArray() {
    String nestedObjectWithArray = "{\"a\":{\"b\":{\"c\":[1,2,3]}}}";

    Map<String, Object> vars = ImmutableMap.of("test", nestedObjectWithArray);
    String template = "{% set obj = test | fromjson %}{{ obj.a.b.c }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("[1, 2, 3]");
  }

  @Test
  public void itRendersArrayOfObjects() {
    String arrayOfObjects = "[{\"a\":1},{\"a\":2},{\"a\": 3}]";

    Map<String, Object> vars = ImmutableMap.of("test", arrayOfObjects);
    String template =
      "{% set obj = test | fromjson %}{{ obj[0].a }} {{ obj[1].a }} {{ obj[2].a }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("1 2 3");
  }
}
