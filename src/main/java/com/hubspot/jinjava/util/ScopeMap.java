package com.hubspot.jinjava.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ScopeMap<K, V> implements Map<K, V> {

  private final Map<K, V> scope;
  private final ScopeMap<K, V> parent;

  public ScopeMap() {
    this(null);
  }

  public ScopeMap(ScopeMap<K, V> parent) {
    this.scope = new HashMap<K, V>();
    this.parent = parent;
  }

  public ScopeMap(ScopeMap<K, V> parent, Map<K, V> scope) {
    this(parent);
    this.scope.putAll(scope);
  }

  public ScopeMap<K, V> getParent() {
    return parent;
  }

  public Map<K, V> getScope() {
    return scope;
  }

  @Override
  public int size() {
    return keySet().size();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    if (scope.containsValue(value)) {
      return true;
    }

    if (parent != null) {
      return parent.containsValue(value);
    }

    return false;
  }

  public V get(Object key, V defVal) {
    V val = get(key);

    if (val != null) {
      return val;
    }

    return defVal;
  }

  @Override
  public V get(Object key) {
    V val = scope.get(key);
    if (val != null) {
      return val;
    }

    if (parent != null) {
      return parent.get(key);
    }

    return null;
  }

  @Override
  public V put(K key, V value) {
    return scope.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return scope.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    scope.putAll(m);
  }

  @Override
  public void clear() {
    scope.clear();
  }

  @Override
  public Set<K> keySet() {
    Set<K> keys = new HashSet<>();

    if (parent != null) {
      keys.addAll(parent.keySet());
    }

    keys.addAll(scope.keySet());

    return keys;
  }

  @Override
  public Collection<V> values() {
    Set<java.util.Map.Entry<K, V>> entrySet = entrySet();
    Collection<V> values = new ArrayList<>(entrySet.size());

    for (Map.Entry<K, V> entry : entrySet) {
      values.add(entry.getValue());
    }

    return values;
  }

  @Override
  @SuppressFBWarnings(justification = "using overridden get() to do scoped retrieve with parent fallback",
      value = "WMI_WRONG_MAP_ITERATOR")
  public Set<java.util.Map.Entry<K, V>> entrySet() {
    Set<java.util.Map.Entry<K, V>> entries = new HashSet<>();

    for (K key : keySet()) {
      entries.add(new ScopeMapEntry<K, V>(key, get(key), this));
    }

    return entries;
  }

  public static class ScopeMapEntry<K, V> implements Map.Entry<K, V> {
    private final Map<K, V> map;
    private final K key;
    private V value;

    public ScopeMapEntry(K key, V value, Map<K, V> map) {
      this.key = key;
      this.value = value;
      this.map = map;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public V setValue(V value) {
      V old = value;
      this.value = value;
      map.put(key, value);
      return old;
    }

  }

}
