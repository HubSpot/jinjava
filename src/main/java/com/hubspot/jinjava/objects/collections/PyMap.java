package com.hubspot.jinjava.objects.collections;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ForwardingMap;
import com.hubspot.jinjava.objects.PyWrapper;

public class PyMap extends ForwardingMap<String, Object> implements PyWrapper {

  private Map<String, Object> map;

  public PyMap(Map<String, Object> map) {
    this.map = map;
  }

  @Override
  protected Map<String, Object> delegate() {
    return map;
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

  public void update(Map<? extends String, ? extends Object> m) {
    putAll(m);
  }

}
