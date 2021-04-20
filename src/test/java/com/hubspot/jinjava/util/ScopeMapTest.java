package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ScopeMapTest {
  Map<String, String> a, b, c;

  @Before
  public void setup() {
    a = ImmutableMap.of("a1", "va1", "a2", "va2");
    b = ImmutableMap.of("a2", "vb2", "a3", "vb3");
    c = ImmutableMap.of("a1", "vc1", "a3", "vc3");
  }

  @Test
  public void noParent() {
    ScopeMap<String, String> map = new ScopeMap<>();
    map.put("a", "v");
    assertThat(map).contains(entry("a", "v"));
    assertThat(map).containsKeys("a");
    assertThat(map).containsValue("v");
  }

  @Test
  public void withParent() {
    ScopeMap<String, String> map = new ScopeMap<String, String>(
      new ScopeMap<>(new ScopeMap<>(null, a), b),
      c
    );

    assertThat(map.get("a1")).isEqualTo("vc1");
    assertThat(map.get("a2")).isEqualTo("vb2");
    assertThat(map.get("a3")).isEqualTo("vc3");

    assertThat(map.get("a5", "foo")).isEqualTo("foo");

    map.put("a4", "vc4");
    assertThat(map).contains(entry("a4", "vc4"));
    assertThat(map.getParent()).doesNotContainKey("a4");

    assertThat(map).hasSize(4);
    assertThat(map.keySet()).containsOnly("a1", "a2", "a3", "a4");
    assertThat(map.values()).containsOnly("vc1", "vb2", "vc3", "vc4");
    assertThat(map)
      .containsOnly(
        entry("a1", "vc1"),
        entry("a2", "vb2"),
        entry("a3", "vc3"),
        entry("a4", "vc4")
      );
  }

  @SuppressWarnings("CollectionAddedToSelf")
  @Test
  public void itDisallowsPuttingItself() {
    ScopeMap<Object, Object> map = new ScopeMap<>();
    assertThatThrownBy(() -> map.put("foo", map))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void itDisallowsPuttingAllWithItselfAsValue() {
    ScopeMap<Object, Object> map1 = new ScopeMap<>();
    ScopeMap<Object, Object> map2 = new ScopeMap<>();
    map2.put("map1", map1);
    assertThatThrownBy(() -> map1.putAll(map2))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
