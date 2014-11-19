package com.hubspot.jinjava.objects.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hubspot.jinjava.objects.PyWrapper;

public class PyMap implements Map<String, Object>, PyWrapper {

  private Map<String, Object> map;
  
  public PyMap(Map<String, Object> map) {
    this.map = map;
  }
  
  public Map<String, Object> toMap() {
    return map;
  }
  
  @Override
  public String toString() {
    return map.toString();
  }
  
  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return map.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    return map.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return map.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    map.putAll(m);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<String> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<Object> values() {
    return map.values();
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return map.entrySet();
  }
  
  public Set<java.util.Map.Entry<String, Object>> items() {
    return entrySet();
  }
  
  public void update(Map<? extends String, ? extends Object> m) {
    putAll(m);
  }
  
}