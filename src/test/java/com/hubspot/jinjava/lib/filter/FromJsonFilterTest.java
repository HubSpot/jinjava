package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class FromJsonFilterTest {
  private static final String NESTED_JSON =
    "{\"first\":[1,2,3],\"nested\":{\"second\":\"string\",\"third\":4}}";
  private JinjavaInterpreter interpreter;
  private FromJsonFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new FromJsonFilter();
  }

  @Test
  public void itReadsStringAsObject() {
    HashMap<String, Object> node = (HashMap<String, Object>) filter.filter(
      NESTED_JSON,
      interpreter
    );
    checkedNestJson(node);
  }

  @Test(expected = InvalidInputException.class)
  public void itFailsWhenStringIsNotJson() {
    String nestedJson = "blah";

    filter.filter(nestedJson, interpreter);
  }

  @Test(expected = InvalidInputException.class)
  public void itFailsWhenParameterIsNotString() {
    Integer nestedJson = 456;

    filter.filter(nestedJson, interpreter);
  }

  @Test
  public void itReadsSafeStringAsObject() {
    SafeString nestedJson = new SafeString(NESTED_JSON);
    HashMap<String, Object> node = (HashMap<String, Object>) filter.filter(
      nestedJson,
      interpreter
    );
    checkedNestJson(node);
  }

  private void checkedNestJson(HashMap<String, Object> node) {
    assertThat(node.get("first")).isEqualTo(Arrays.asList(1, 2, 3));

    HashMap<String, Object> nested = (HashMap<String, Object>) node.get("nested");
    assertThat(nested.get("second")).isEqualTo("string");
    assertThat(nested.get("third")).isEqualTo(4);
  }
}
