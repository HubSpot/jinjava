package com.hubspot.jinjava.objects.collections;

import com.google.common.collect.ForwardingList;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PyList extends ForwardingList<Object> implements PyWrapper {
  private List<Object> list;

  public PyList(List<Object> list) {
    this.list = list;
  }

  @Override
  protected List<Object> delegate() {
    return list;
  }

  public List<Object> toList() {
    return list;
  }

  public boolean append(Object e) {
    return add(e);
  }

  public void insert(int i, Object e) {
    add(Math.min(list.size(), i), e);
  }

  public boolean extend(PyList e) {
    return addAll(e.list);
  }

  public Object pop() {
    return list.size() == 0 ? null : remove(list.size() - 1);
  }

  public Object pop(int index) {
    return index >= list.size() ? null : remove(index);
  }

  public long count(Object o) {
    return stream().filter(object -> Objects.equals(object, o)).count();
  }

  public void reverse() {
    Collections.reverse(list);
  }

  public PyList copy() {
    return new PyList(new ArrayList<>(list));
  }

  public int index(Object o) {
    return indexOf(o);
  }

  public int index(Object o, int begin, int end) {
    for (int i = begin; i < end; i++) {
      if (Objects.equals(o, get(i))) {
        return i;
      }
    }
    return -1;
  }
}
