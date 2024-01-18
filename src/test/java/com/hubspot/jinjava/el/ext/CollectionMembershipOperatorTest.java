package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class CollectionMembershipOperatorTest {

  static class NoKeySetMap<K, V> extends AbstractMap<K, V> {

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public Set<K> keySet() {
      return Collections.emptySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      return Collections.emptySet();
    }
  }

  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
  }

  @Test
  public void itChecksIfStringContainsChar() {
    assertThat(interpreter.resolveELExpression("'a' in 'pastrami'", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("'o' in 'pastrami'", -1)).isEqualTo(false);
  }

  @Test
  public void itChecksIfArrayContainsValue() {
    assertThat(interpreter.resolveELExpression("11 in [11, 12, 13]", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("14 in [11, 12, 13]", -1))
      .isEqualTo(false);
  }

  @Test
  public void itChecksIfDictionaryContainsKey() {
    assertThat(interpreter.resolveELExpression("'a' in {'a': 1, 'b': 2}", -1))
      .isEqualTo(true);
    assertThat(interpreter.resolveELExpression("'c' in {'a': 1, 'b': 2}", -1))
      .isEqualTo(false);
  }

  @Test
  public void itChecksIfDictionaryContainsNullKey() {
    Map<String, Object> map = new HashMap();
    map.put(null, "null");
    map.put("a", 1);
    interpreter.getContext().put("map", map);
    assertThat(interpreter.resolveELExpression("'a' in map", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("null in map", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("'b' in map", -1)).isEqualTo(false);
  }

  @Test
  public void itCheckEmptyKeySet() {
    // The map is "not" empty, but keySet() is empty.
    Map<String, Object> map = new NoKeySetMap<>();
    interpreter.getContext().put("map", map);
    assertThat(interpreter.resolveELExpression("'a' in map", -1)).isEqualTo(false);
  }
}
