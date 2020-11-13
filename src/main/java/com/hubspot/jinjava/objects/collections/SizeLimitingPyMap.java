package com.hubspot.jinjava.objects.collections;

import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.HashSet;
import java.util.Map;

public class SizeLimitingPyMap extends PyMap implements PyWrapper {
  private int maxSize;

  private SizeLimitingPyMap(Map<String, Object> map) {
    super(map);
  }

  public SizeLimitingPyMap(Map<String, Object> map, int maxSize) {
    super(map);
    if (maxSize <= 0) {
      throw new IllegalArgumentException("maxSize must be >= 1");
    }

    this.maxSize = maxSize;
    checkSize(map.size());
  }

  @Override
  public Object put(String s, Object o) {
    if (!delegate().containsKey(s)) {
      checkSize(delegate().size() + 1);
    }

    return super.put(s, o);
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {
    HashSet<String> keys = new HashSet<>(delegate().keySet());
    checkSize(
      (int) m.keySet().stream().filter(k -> !keys.contains(k)).count() + delegate().size()
    );
    super.putAll(m);
  }

  private void checkSize(int newSize) {
    if (newSize > maxSize) {
      throw new CollectionTooBigException(newSize, maxSize);
    }
  }
}
