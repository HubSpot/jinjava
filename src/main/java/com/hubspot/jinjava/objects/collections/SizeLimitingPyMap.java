package com.hubspot.jinjava.objects.collections;

import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.HashSet;
import java.util.Map;

public class SizeLimitingPyMap extends PyMap implements PyWrapper {

  private int maxSize;
  private boolean hasWarned;

  private SizeLimitingPyMap(Map<String, Object> map) {
    super(map);
  }

  public SizeLimitingPyMap(Map<String, Object> map, int maxSize) {
    super(map);
    if (map == null) {
      throw new IllegalArgumentException("map is null");
    }
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
    if (m == null) {
      return;
    }
    HashSet<String> keys = new HashSet<>(delegate().keySet());
    checkSize(
      (int) m.keySet().stream().filter(k -> !keys.contains(k)).count() + delegate().size()
    );
    super.putAll(m);
  }

  private void checkSize(int newSize) {
    if (newSize > maxSize) {
      throw new CollectionTooBigException(newSize, maxSize);
    } else if (!hasWarned && newSize >= maxSize * 0.9) {
      hasWarned = true;
      JinjavaInterpreter
        .getCurrent()
        .addError(
          new TemplateError(
            ErrorType.WARNING,
            ErrorReason.COLLECTION_TOO_BIG,
            String.format("Map is at 90%% of max size (%d of %d)", newSize, maxSize),
            null,
            -1,
            -1,
            new CollectionTooBigException(newSize, maxSize)
          )
        );
    }
  }
}
