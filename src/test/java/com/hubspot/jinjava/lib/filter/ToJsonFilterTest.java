package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ToJsonFilterTest extends BaseInterpretingTest {
  private ToJsonFilter filter;

  @Before
  public void setup() {
    filter = new ToJsonFilter();
  }

  @Test
  public void itWritesObjectAsString() {
    int[] testArray = new int[] { 4, 1, 2 };
    assertThat(filter.filter(testArray, interpreter)).isEqualTo("[4,1,2]");

    Map<String, Object> testMap = new LinkedHashMap<>();
    testMap.put("testArray", testArray);
    testMap.put("testString", "testString");
    assertThat(filter.filter(testMap, interpreter))
      .isEqualTo("{\"testArray\":[4,1,2],\"testString\":\"testString\"}");
  }

  @Test
  public void itWritesObjectWithOrderedKeyAsString() {
    int[] testArray = new int[] { 4, 1, 2 };
    Map<String, Object> testMap = new HashMap<>();
    testMap.put("testString", "testString");
    testMap.put("testArray", testArray);
    testMap.put("another", "ab");
    Map<String, Object> nested = new HashMap<>();
    nested.put("innerKey2", "v2");
    nested.put("innerKey1", "v1");
    testMap.put("nested", nested);
    assertThat(filter.filter(testMap, interpreter))
      .isEqualTo(
        "{\"another\":\"ab\",\"nested\":{\"innerKey1\":\"v1\",\"innerKey2\":\"v2\"},\"testArray\":[4,1,2],\"testString\":\"testString\"}"
      );
  }

  @Test
  public void itWritesObjectWithoutOrderedKeyAsString() {
    int[] testArray = new int[] { 4, 1, 2 };
    Map<String, Object> testMap = new HashMap<>();
    testMap.put("testString", "testString");
    testMap.put("testArray", testArray);
    testMap.put("another", "ab");
    Map<String, Object> nested = new HashMap<>();
    nested.put("innerKey2", "v2");
    nested.put("innerKey1", "v1");
    testMap.put("nested", nested);

    Jinjava jinjava = new Jinjava(
      JinjavaConfig.newBuilder().withObjectMapper(new ObjectMapper()).build()
    );
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    assertThat(filter.filter(testMap, interpreter))
      .isEqualTo(
        """
{"testArray":[4,1,2],"another":"ab","testString":"testString","nested":{"innerKey2":"v2","innerKey1":"v1"}}"""
        );
  }
}
