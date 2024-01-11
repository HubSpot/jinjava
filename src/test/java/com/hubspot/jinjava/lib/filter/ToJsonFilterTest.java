package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
  public void itLimitsLength() {
    List<List<?>> original = new ArrayList<>();
    List<List<?>> temp = original;
    for (int i = 0; i < 100; i++) {
      List<List<?>> nested = new ArrayList<>();
      temp.add(nested);
      temp = nested;
    }
    interpreter =
      new Jinjava(JinjavaConfig.newBuilder().withMaxOutputSize(500).build())
        .newInterpreter();
    assertThat(filter.filter(original, interpreter)).asString().contains("[[]]]]");
    for (int i = 0; i < 400; i++) {
      List<List<?>> nested = new ArrayList<>();
      temp.add(nested);
      temp = nested;
    }
    try {
      filter.filter(original, interpreter);
    } catch (Exception e) {
      assertThat(e).isInstanceOf(OutputTooBigException.class);
    }
  }
}
