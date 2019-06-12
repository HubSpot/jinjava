package com.hubspot.jinjava.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

public class DummyObject implements Map<String, Object>, PyWrapper {

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
  public Object put(String key, Object value) {
    return new DummyObject();
  }

  @Override
  public Object remove(Object key) {
    return new DummyObject();
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {

  }

  @Override
  public void clear() {

  }

  @Override
  public Set<String> keySet() {
    return null;
  }

  @Override
  public Collection<Object> values() {
    return ImmutableList.of(new DummyObject());
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return null;
  }
}
