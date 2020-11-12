package com.hubspot.jinjava.objects.collections;

import com.hubspot.jinjava.interpret.IndexOutOfRangeException;
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
    if (map.size() > maxSize) {
      throw createOutOfRangeException(map.size());
    }
  }

  @Override
  public Object put(String s, Object o) {
    if (delegate().size() + 1 > maxSize && !delegate().containsKey(s)) {
      throw createOutOfRangeException(delegate().size() + 1);
    }

    return super.put(s, o);
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {
    HashSet<String> keys = new HashSet<>(delegate().keySet());
    int newKeys = (int) m.keySet().stream().filter(k -> !keys.contains(k)).count();

    if (newKeys + delegate().size() > maxSize) {
      throw createOutOfRangeException(newKeys + delegate().size());
    }

    super.putAll(m);
  }

  IndexOutOfRangeException createOutOfRangeException(int index) {
    return new IndexOutOfRangeException(
      String.format("%d is out of range for map of maximum size %d", index, maxSize)
    );
  }
}
