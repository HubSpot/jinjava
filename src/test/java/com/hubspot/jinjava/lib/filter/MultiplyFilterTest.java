package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.RenderResult;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class MultiplyFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(MultiplyFilter.class);
  }

  @Test
  public void itMultipliesDecimalNumbers() {
    Map<String, Object> vars = ImmutableMap.of("test", 10);
    RenderResult renderResult = jinjava.renderForResult("{{ test|multiply(.25) }}", vars);
    assertThat(renderResult.getOutput()).isEqualTo("2.5");
  }

  @Test
  public void itCoercesStringsToNumbers() {
    Map<String, Object> vars = ImmutableMap.of("test", "10");
    RenderResult renderResult = jinjava.renderForResult("{{ test|multiply(.25) }}", vars);
    assertThat(renderResult.getOutput()).isEqualTo("2.5");
  }
}
