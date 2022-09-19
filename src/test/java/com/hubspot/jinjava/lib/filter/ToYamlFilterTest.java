package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ToYamlFilterTest extends BaseInterpretingTest {
  private ToYamlFilter filter;

  @Before
  public void setup() {
    filter = new ToYamlFilter();
  }

  @Test
  public void itWritesObjectAsString() {
    int[] testArray = new int[] { 4, 1, 2 };
    assertThat(filter.filter(testArray, interpreter))
      .isEqualTo("- 4\n" + "- 1\n" + "- 2\n");

    Map<String, Object> testMap = new LinkedHashMap<>();
    testMap.put("testArray", testArray);
    testMap.put("testString", "testString");
    assertThat(filter.filter(testMap, interpreter))
      .isEqualTo(
        "testArray:\n" + "- 4\n" + "- 1\n" + "- 2\n" + "testString: \"testString\"\n"
      );
  }
}
