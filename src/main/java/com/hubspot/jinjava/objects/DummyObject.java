package com.hubspot.jinjava.objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DummyObject implements Map<Object, Object>, PyWrapper {

  @Override
  public int size() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return true;
  }

  @Override
  public boolean containsValue(Object value) {
    return true;
  }

  @Override
  public Object get(Object key) {
    return new DummyObject();
  }

  @Override
  public Object put(Object key, Object value) {
    return new DummyObject();
  }

  @Override
  public Object remove(Object key) {
    return new DummyObject();
  }

  @Override
  public void putAll(Map<? extends Object, ?> m) {}

  @Override
  public void clear() {}

  @Override
  public Set<Object> keySet() {
    return ImmutableSet.of(new DummyObject());
  }

  @Override
  public Collection<Object> values() {
    return ImmutableList.of(new DummyObject());
  }

  @Override
  public Set<Entry<Object, Object>> entrySet() {
    return ImmutableSet.of(Map.entry(new DummyObject(), new DummyObject()));
  }
}
