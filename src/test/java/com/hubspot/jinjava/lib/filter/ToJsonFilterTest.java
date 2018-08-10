package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ToJsonFilterTest {

  private JinjavaInterpreter interpreter;
  private ToJsonFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new ToJsonFilter();
  }

  @Test
  public void itWritesObjectAsString() {

    int[] testArray = new int[] {4, 1, 2};
    assertThat(filter.filter(testArray, interpreter)).isEqualTo("[4,1,2]");

    Map<String, Object> testMap = new HashMap<>();
    testMap.put("testArray", testArray);
    testMap.put("testString", "testString");
    assertThat(filter.filter(testMap, interpreter)).isEqualTo("{\"testArray\":[4,1,2],\"testString\":\"testString\"}");
  }
}
