package com.hubspot.jinjava.lib.filter;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FromJsonFilterTest {
  private static final String TRIVIAL_JSON_ARRAY = "[\"one\",\"two\",\"three\"]";
  private static final String NESTED_JSON =
    "{\"first\":[1,2,3],\"nested\":{\"second\":\"string\",\"third\":4}}";
  private static final String DEEPLY_NESTED_ARRAY = "{\"a\":{\"b\":{\"c\": [1,2,3]}}}";
  private static final String EMPTY_JSON_OBJECT = "{}";
  private static final String EMPTY_JSON_ARRAY = "[]";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JinjavaInterpreter interpreter;
  private FromJsonFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new FromJsonFilter();
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
  public void itReadsEmptyJsonObjectString() {
    JsonNode node = (JsonNode) filter.filter(EMPTY_JSON_OBJECT, interpreter);
    assertThat(node.elements().hasNext()).isEqualTo(false);
  }

  @Test
  public void itReadsStringAsObject() {
    JsonNode node = (JsonNode) filter.filter(NESTED_JSON, interpreter);

    checkNestedJson(node);
  }

  @Test
  public void itReadsSafeStringAsObject() {
    SafeString nestedJson = new SafeString(NESTED_JSON);
    JsonNode node = (JsonNode) filter.filter(nestedJson, interpreter);

    checkNestedJson(node);
  }

  @Test
  public void itReadsEmptyJsonArrayString() {
    JsonNode node = (JsonNode) filter.filter(EMPTY_JSON_ARRAY, interpreter);
    assertThat(node.elements().hasNext()).isEqualTo(false);
  }

  @Test
  public void itReadsStringAsList() {
    JsonNode node = (JsonNode) filter.filter(TRIVIAL_JSON_ARRAY, interpreter);

    List<String> nodeAsList = OBJECT_MAPPER.convertValue(node, List.class);
    assertThat(nodeAsList.toArray())
      .containsExactly(Arrays.asList("one", "two", "three").toArray());
  }

  @Test
  public void itReadsSafeStringArrayAsObject() {
    SafeString arrayJson = new SafeString(TRIVIAL_JSON_ARRAY);
    JsonNode node = (JsonNode) filter.filter(arrayJson, interpreter);

    List<String> nodeAsList = OBJECT_MAPPER.convertValue(node, List.class);
    assertThat(nodeAsList.toArray())
      .containsExactly(Arrays.asList("one", "two", "three").toArray());
  }

  @Test
  public void itReadsDeeplyNestedArrayString() {
    JsonNode node = (JsonNode) filter.filter(DEEPLY_NESTED_ARRAY, interpreter);
    JsonNode target = node.get("a").get("b").get("c");

    List<String> targetAsList = OBJECT_MAPPER.convertValue(target, List.class);
    assertThat(targetAsList.toArray()).containsExactly(Arrays.asList(1, 2, 3).toArray());
  }

  private void checkNestedJson(JsonNode node) {
    assertThat(node.get("first").isArray());

    List<Integer> firstFieldValue = OBJECT_MAPPER.convertValue(
      node.get("first"),
      List.class
    );
    assertThat(firstFieldValue.toArray())
      .containsExactly(Arrays.asList(1, 2, 3).toArray());

    JsonNode nested = node.get("nested");
    assertThat(nested.get("second").asText()).isEqualTo("string");
    assertThat(nested.get("third").asInt()).isEqualTo(4);
  }
}
