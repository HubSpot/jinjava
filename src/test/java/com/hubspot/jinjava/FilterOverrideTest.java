package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.testobjects.FilterOverrideTestObjects;
import java.util.HashMap;
import org.junit.Test;

public class FilterOverrideTest {

  @Test
  public void itAllowsUsersToOverrideBuiltInFilters() {
    Jinjava jinjava = new Jinjava(BaseJinjavaTest.newConfigBuilder().build());
    String template = "{{ 5 | add(6) }}";

    assertThat(jinjava.render(template, new HashMap<>())).isEqualTo("11");

    jinjava
      .getGlobalContext()
      .registerClasses(FilterOverrideTestObjects.DescriptiveAddFilter.class);
    assertThat(jinjava.render(template, new HashMap<>())).isEqualTo("5 + 6 = 11");
  }
}
