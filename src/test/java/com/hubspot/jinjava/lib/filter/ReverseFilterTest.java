package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.LegacyOverrides;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ReverseFilterTest extends BaseJinjavaTest {

  @Test
  public void itReversesPrimitiveIntArray() {
    Map<String, Object> context = new HashMap<>();
    context.put("arr", new int[] { 1, 2, 3 });
    assertThat(
      jinjava.render("{% for item in arr|reverse %}{{ item }}{% endfor %}", context)
    )
      .isEqualTo("321");
  }

  @Test
  public void itReversesObjectArray() {
    Map<String, Object> context = new HashMap<>();
    context.put("arr", new String[] { "a", "b", "c" });
    assertThat(
      jinjava.render("{% for item in arr|reverse %}{{ item }}{% endfor %}", context)
    )
      .isEqualTo("cba");
  }

  @Test
  public void itAllowsIndexingWhenLegacyOverrideIsDisabled() {
    Jinjava legacyJinjava = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withLegacyOverrides(
          LegacyOverrides.Builder
            .from(LegacyOverrides.THREE_POINT_0)
            .withIteratorOnlyReverseFilter(false)
            .build()
        )
        .build()
    );
    Map<String, Object> context = new HashMap<>();
    context.put("arr", new String[] { "a", "b", "c" });
    assertThat(legacyJinjava.render("{{ (arr|reverse)[0] }}", context)).isEqualTo("c");
  }
}
