package com.hubspot.jinjava.lib.filter;

import java.util.HashMap;
import java.util.Map;

import com.hubspot.jinjava.BaseInterpretingTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ToYamlFilterTest extends BaseInterpretingTest {
  private ToYamlFilter filter;

  @Before
  public void setup() {
    filter = new ToYamlFilter();
  }

  @Test
  public void itWritesObjectAsString() {
    int[] testArray = new int[] { 4, 1, 2 };
    assertThat(filter.filter(testArray, interpreter)).isEqualTo("- 4\n" +
                                                                "- 1\n" +
                                                                "- 2\n");

    Map<String, Object> testMap = new HashMap<>();
    testMap.put("testArray", testArray);
    testMap.put("testString", "testString");
    assertThat(filter.filter(testMap, interpreter))
      .isEqualTo("testArray:\n" +
                 "- 4\n" +
                 "- 1\n" +
                 "- 2\n" +
                 "testString: \"testString\"\n"
                 );
  }
}
