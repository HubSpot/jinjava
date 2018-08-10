package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


public class FromJsonFilterTest {

  private JinjavaInterpreter interpreter;
  private FromJsonFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new FromJsonFilter();
  }

  @Test
  public void itReadsStringAsObject() {

    String nestedJson = "{\"first\":[1,2,3],\"nested\":{\"second\":\"string\",\"third\":4}}";

    HashMap<String, Object> node = (HashMap<String, Object>) filter.filter(nestedJson, interpreter);
    assertThat(node.get("first")).isEqualTo(Arrays.asList(1, 2, 3));

    HashMap<String, Object> nested = (HashMap<String, Object>) node.get("nested");
    assertThat(nested.get("second")).isEqualTo("string");
    assertThat(nested.get("third")).isEqualTo(4);
  }

  @Test(expected = InterpretException.class)
  public void itFailsWhenStringIsNotJson() {

    String nestedJson = "blah";

    filter.filter(nestedJson, interpreter);
  }

  @Test(expected = InterpretException.class)
  public void itFailsWhenParameterIsNotString() {

    Integer nestedJson = 456;

    filter.filter(nestedJson, interpreter);
  }
}
