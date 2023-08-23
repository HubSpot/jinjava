package com.hubspot.jinjava.objects.collections;

import com.google.common.collect.ForwardingMap;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class PyMap extends ForwardingMap<String, Object> implements PyWrapper {
  private final ThreadLocal<Semaphore> semaphore = ThreadLocal.withInitial(
    () -> new Semaphore(1)
  );

  private final Map<String, Object> map;

  public PyMap(Map<String, Object> map) {
    this.map = map;
  }

  @Override
  protected Map<String, Object> delegate() {
    return map;
  }

  @Override
  public Object put(String s, Object o) {
    if (o == this) {
      throw new IllegalArgumentException("Can't add map object to itself");
    }
    return delegate().put(s, o);
  }

  @Override
  public String toString() {
    return delegate().toString();
  }

  public Map<String, Object> toMap() {
    return map;
  }

  public Set<java.util.Map.Entry<String, Object>> items() {
    return entrySet();
  }

  public Set<String> keys() {
    return keySet();
  }

  public void update(Map<? extends String, ? extends Object> m) {
    if (m == this) {
      throw new IllegalArgumentException("Can't update map object with itself");
    }
    putAll(m);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    if (m == this) {
      throw new IllegalArgumentException(
        "Map putAll() operation can't be used to add map to itself"
      );
    }
    super.putAll(m);
  }

  @Override
  public int hashCode() {
    if (semaphore.get().tryAcquire()) {
      try {
        return super.hashCode();
      } finally {
        semaphore.get().release();
      }
    } else {
      return Objects.hashCode(null);
    }
  }
}
