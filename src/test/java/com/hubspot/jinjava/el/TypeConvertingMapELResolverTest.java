package com.hubspot.jinjava.el;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class TypeConvertingMapELResolverTest {
  private TypeConvertingMapELResolver typeConvertingMapELResolver;

  @Before
  public void setup() {
    typeConvertingMapELResolver = new TypeConvertingMapELResolver(false);
  }

  @Test
  public void itResolvesProperties() {
    Map<String, String> values = ImmutableMap.of("1", "value1", "2", "value2");
    assertThat(typeConvertingMapELResolver.getValue(new JinjavaELContext(), values, "2"))
      .isEqualTo("value2");
  }

  @Test
  public void itConvertsPropertyClassWhenResolvingProperty() {
    Map<String, String> values = ImmutableMap.of("1", "value1", "2", "value2");
    assertThat(typeConvertingMapELResolver.getValue(new JinjavaELContext(), values, 1))
      .isEqualTo("value1");
  }

  @Test
  public void itHandlesNullKeyValuesWhenResolvingProperty() {
    Map<String, String> values = new HashMap<>();
    values.put(null, "nullValue");
    values.put("1", "value1");
    values.put("2", "value2");
    assertThat(typeConvertingMapELResolver.getValue(new JinjavaELContext(), values, 1))
      .isEqualTo("value1");
  }

  @Test
  public void itHandlesMapWithOnlyNullKey() {
    Map<String, String> values = new HashMap<>();
    values.put(null, "nullValue");
    assertThat(typeConvertingMapELResolver.getValue(new JinjavaELContext(), values, 1))
      .isEqualTo(null);
  }

  @Test
  public void itResolvesNullPropertyValue() {
    Map<String, String> values = new HashMap<>();
    values.put(null, "nullValue");
    values.put("1", "value1");
    values.put("2", "value2");
    assertThat(typeConvertingMapELResolver.getValue(new JinjavaELContext(), values, null))
      .isEqualTo("nullValue");
  }
}
