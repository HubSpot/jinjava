package com.hubspot.jinjava.objects.collections;

import com.google.common.collect.ForwardingMap;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class PyMap extends ForwardingMap<String, Object> implements PyWrapper {
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
    int h = 0;
    List<Object> valueList = new ArrayList<>(map.entrySet());
    ListIterator<Object> valueIterator = valueList.listIterator();
    Set<Integer> visited = new HashSet<>();
    while (valueIterator.hasNext()) {
      Object next = valueIterator.next();
      int code = System.identityHashCode(next);
      if (visited.contains(code)) {
        continue;
      } else {
        visited.add(code);
      }
      if (next instanceof Entry) {
        Entry nextEntry = (Entry) next;
        if (nextEntry.getKey() != null) {
          h += nextEntry.getKey().hashCode();
        }
        valueIterator.add(nextEntry.getValue());
        valueIterator.previous();
      } else if (next instanceof Iterable) {
        for (Object o : (Iterable) next) {
          valueIterator.add(o);
          valueIterator.previous();
        }
      } else if (next instanceof Map) {
        ((Map) next).entrySet()
          .forEach(
            e -> {
              valueIterator.add(e);
              valueIterator.previous();
            }
          );
      } else if (next != null) {
        h += next.hashCode();
      }
    }
    return h;
  }
}
