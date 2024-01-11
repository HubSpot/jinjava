package com.hubspot.jinjava.objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Before;
import org.junit.Test;

public class NamespaceTest {

  private Namespace namespace;

  @Before
  public void setup() {
    namespace = new Namespace();
  }

  @Test
  public void shouldReturnNullIfValueDoNotExists() {
    String key = "key";
    Object result = namespace.get(key);
    assertThat(result).isEqualTo(null);
  }

  @Test
  public void shouldReplaceValueForKeyIfValueForKeyExists() {
    String key = "key";
    namespace.put(key, Boolean.TRUE);
    namespace.put(key, "second value");

    Object result = namespace.get(key);
    assertThat(result).isEqualTo("second value");
  }

  @Test
  public void shouldSetValueIfValueDoesNotExists() {
    String key = "key";
    String value = "Test";
    namespace.put(key, value);
    assertThat(namespace.get(key)).isEqualTo(value);
  }
}
