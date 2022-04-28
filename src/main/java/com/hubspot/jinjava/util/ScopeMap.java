package com.hubspot.jinjava.util;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class ScopeMap<K, V> implements Map<K, V> {
  private final Map<K, V> scope;
  private final ScopeMap<K, V> parent;

  public ScopeMap() {
    this(null);
  }

  public ScopeMap(ScopeMap<K, V> parent) {
    this.scope = new HashMap<>();
    this.parent = parent;

    Set<ScopeMap<K, V>> parents = new HashSet<>();
    if (parent != null) {
      ScopeMap<K, V> p = parent.getParent();
      while (p != null) {
        parents.add(p);
        if (parents.contains(parent)) {
          ENGINE_LOG.error(
            "Parent loop detected:\n{}",
            Arrays
              .stream(Thread.currentThread().getStackTrace())
              .map(StackTraceElement::toString)
              .collect(Collectors.joining("\n"))
          );
          break;
        }
        p = p.getParent();
      }
    }
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
    if (value == this) {
      throw new IllegalArgumentException(
        String.format("attempt to put on map with key '%s' and value of itself", key)
      );
    }
    return scope.put(key, value);
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    boolean replaced = scope.replace(key, oldValue, newValue);
    if (replaced) {
      return true;
    }
    if (parent != null) {
      return parent.replace(key, oldValue, newValue);
    }
    return false;
  }

  @Override
  public V replace(K key, V value) {
    V val = scope.replace(key, value);
    if (val != null) {
      return val;
    }
    if (parent != null) {
      return parent.replace(key, value);
    }
    return null;
  }

  @Override
  public V remove(Object key) {
    return scope.remove(key);
  }

  @Override
  public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
    for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
      if (entry.getValue() == this) {
        throw new IllegalArgumentException(
          String.format(
            "attempt to putAll on map with key '%s' and value of itself",
            entry.getKey()
          )
        );
      }
    }

    scope.putAll(m);
  }

  @Override
  public void clear() {
    scope.clear();
  }

  @Override
  @Nonnull
  public Set<K> keySet() {
    Set<K> keys = new HashSet<>();

    if (parent != null) {
      keys.addAll(parent.keySet());
    }

    keys.addAll(scope.keySet());

    return keys;
  }

  @Override
  @Nonnull
  public Collection<V> values() {
    Set<java.util.Map.Entry<K, V>> entrySet = entrySet();
    Collection<V> values = new ArrayList<>(entrySet.size());

    for (Map.Entry<K, V> entry : entrySet) {
      values.add(entry.getValue());
    }

    return values;
  }

  @Override
  @SuppressFBWarnings(
    justification = "using overridden get() to do scoped retrieve with parent fallback",
    value = "WMI_WRONG_MAP_ITERATOR"
  )
  @Nonnull
  public Set<java.util.Map.Entry<K, V>> entrySet() {
    Set<java.util.Map.Entry<K, V>> entries = new HashSet<>();

    for (K key : keySet()) {
      entries.add(new ScopeMapEntry<>(key, get(key), this));
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
      this.value = value;
      map.put(key, value);
      return value;
    }
  }
}
