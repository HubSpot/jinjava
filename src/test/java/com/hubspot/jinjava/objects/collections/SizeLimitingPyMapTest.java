package com.hubspot.jinjava.objects.collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;

public class SizeLimitingPyMapTest extends BaseInterpretingTest {

  @Test
  public void itDoesntLimitByDefault() {
    SizeLimitingPyMap objects = new SizeLimitingPyMap(createMap(10), Integer.MAX_VALUE);
    assertThat(objects.size()).isEqualTo(10);
  }

  @Test(expected = CollectionTooBigException.class)
  public void itLimitsOnCreate() {
    new SizeLimitingPyMap(createMap(3), 2);
  }

  @Test
  public void itWarnsOnPut() {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    int i = 8;
    SizeLimitingPyMap objects = new SizeLimitingPyMap(createMap(i), 10);
    assertThat(interpreter.getErrors()).isEmpty();

    objects.put(Objects.toString(i + 1), ++i);
    assertThat(interpreter.getErrors().size()).isEqualTo(1);
    assertThat(interpreter.getErrors().get(0).getException())
      .isInstanceOf(CollectionTooBigException.class);

    objects.put(Objects.toString(i + 1), ++i);

    assertThatThrownBy(() -> objects.put(Objects.toString(11), 11))
      .isInstanceOf(CollectionTooBigException.class);
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
      .isInstanceOf(CollectionTooBigException.class);
  }

  @Test
  public void itLimitsOnAddAll() {
    SizeLimitingPyMap objects = new SizeLimitingPyMap(new HashMap<>(), 2);
    assertThatThrownBy(() -> objects.putAll(createMap(3)))
      .isInstanceOf(CollectionTooBigException.class);
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
