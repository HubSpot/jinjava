package com.hubspot.jinjava.objects.collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.IndexOutOfRangeException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class SizeLimitingPyMapTest extends BaseJinjavaTest {

  @Test
  public void itDoesntLimitByDefault() {
    SizeLimitingPyMap objects = new SizeLimitingPyMap(createMap(10), Integer.MAX_VALUE);
    assertThat(objects.size()).isEqualTo(10);
  }

  @Test(expected = IndexOutOfRangeException.class)
  public void itLimitsOnCreate() {
    new SizeLimitingPyMap(createMap(3), 2);
  }

  @Test
  public void itLimitsOnPut() {
    SizeLimitingPyMap objects = new SizeLimitingPyMap(new HashMap<>(), 2);
    objects.put("1", "foo");
    objects.put("2", "foo");
    objects.put("1", "foo");
    objects.put("2", "foo");
    objects.put("2", "foo");
    objects.put("2", "foo");
    assertThatThrownBy(() -> objects.put("3", "foo"))
      .isInstanceOf(IndexOutOfRangeException.class);
  }

  @Test
  public void itLimitsOnAddAll() {
    SizeLimitingPyMap objects = new SizeLimitingPyMap(new HashMap<>(), 2);
    assertThatThrownBy(() -> objects.putAll(createMap(3)))
      .isInstanceOf(IndexOutOfRangeException.class);
  }

  @Test
  public void itIgnoresLimitsForDuplicateKeys() {
    SizeLimitingPyMap objects = new SizeLimitingPyMap(new HashMap<>(), 2);
    objects.putAll(createMap(2));
    assertThat(objects.size()).isEqualTo(2);
    objects.putAll(createMap(2));
    assertThat(objects.size()).isEqualTo(2);
  }

  private static Map<String, Object> createMap(int size) {
    Map<String, Object> result = new HashMap<>();
    for (int i = 0; i < size; i++) {
      result.put(String.valueOf(i + 1), i + 1);
    }
    return result;
  }
}
