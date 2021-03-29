package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.InvalidInputException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FromYamlFilterTest extends BaseInterpretingTest {
  private FromYamlFilter filter;

  @Before
  public void setup() {
    filter = new FromYamlFilter();
  }

  @Test(expected = InvalidInputException.class)
  public void itFailsWhenParameterIsNotString() {
    Integer json = 456;

    filter.filter(json, interpreter);
  }

  @Test(expected = InvalidInputException.class)
  public void itFailsWhenYamlIsInvalid() {
    String json = "a: b:";

    filter.filter(json, interpreter);
  }

  @Test
  public void itRendersTrivialYamlObject() {
    String trivialYamlObject = "a: 100\n" + "b: 200";

    Map<String, Object> vars = ImmutableMap.of("test", trivialYamlObject);
    String template = "{% set obj = test | fromyaml %}{{ obj.a }} {{ obj.b }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("100 200");
  }

  @Test
  public void itRendersTrivialYamlArray() {
    String trivialYamlArray = "- one\n" + "- two\n" + "- three";

    Map<String, Object> vars = ImmutableMap.of("test", trivialYamlArray);
    String template =
      "{% set obj = test | fromyaml %}{{ obj[0] }} {{ obj[1] }} {{ obj[2] }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("one two three");
  }

  @Test
  public void itRendersNestedObjectYaml() {
    String nestedObject =
      "first: 1\n" + "nested:\n" + "  second: string\n" + "  third: 4";

    Map<String, Object> vars = ImmutableMap.of("test", nestedObject);
    String template =
      "{% set obj = test | fromyaml %}{{ obj.first }} {{ obj.nested.second }} {{ obj.nested.third }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("1 string 4");
  }

  @Test
  public void itRendersNestedYamlWithArray() {
    String nestedObjectWithArray =
      "a:\n" + "  b:\n" + "    c:\n" + "    - 1\n" + "    - 2\n" + "    - 3";

    Map<String, Object> vars = ImmutableMap.of("test", nestedObjectWithArray);
    String template = "{% set obj = test | fromyaml %}{{ obj.a.b.c }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("[1, 2, 3]");
  }

  @Test
  public void itRendersArrayOfObjects() {
    String arrayOfObjects = "- a: 1\n" + "- a: 2\n" + "- a: 3";

    Map<String, Object> vars = ImmutableMap.of("test", arrayOfObjects);
    String template =
      "{% set obj = test | fromyaml %}{{ obj[0].a }} {{ obj[1].a }} {{ obj[2].a }}";
    String renderedJinjava = jinjava.render(template, vars);

    assertThat(renderedJinjava).isEqualTo("1 2 3");
  }
}
