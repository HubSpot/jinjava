package com.hubspot.jinjava.objects.collections;

import com.google.common.collect.ForwardingList;
import com.hubspot.jinjava.interpret.IndexOutOfRangeException;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class PyList extends ForwardingList<Object> implements PyWrapper {
  private final ThreadLocal<Semaphore> semaphore = ThreadLocal.withInitial(
    () -> new Semaphore(1)
  );
  private final List<Object> list;

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
    if (this == e) {
      return false;
    }
    return add(e);
  }

  public void insert(int i, Object e) {
    if (this == e) {
      return;
    }
    if (i >= list.size()) {
      throw createOutOfRangeException(i);
    }

    if (i < 0) {
      i = Math.max(0, list.size() + i);
    }

    add(i, e);
  }

  public boolean extend(PyList e) {
    return e != null && addAll(e.list);
  }

  public Object pop() {
    if (list.size() == 0) {
      throw createOutOfRangeException(0);
    }
    return remove(list.size() - 1);
  }

  public Object pop(int index) {
    if (Math.abs(index) >= list.size()) {
      throw createOutOfRangeException(index);
    }

    if (index < 0) {
      index = list.size() + index;
    }

    return remove(index);
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

  IndexOutOfRangeException createOutOfRangeException(int index) {
    return new IndexOutOfRangeException(
      String.format("Index %d is out of range for list of size %d", index, list.size())
    );
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
