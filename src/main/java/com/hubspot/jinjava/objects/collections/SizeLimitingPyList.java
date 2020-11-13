package com.hubspot.jinjava.objects.collections;

import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SizeLimitingPyList extends PyList implements PyWrapper {
  private int maxSize;

  private SizeLimitingPyList(List<Object> list) {
    super(list);
  }

  public SizeLimitingPyList(List<Object> list, int maxSize) {
    super(list);
    if (maxSize <= 0) {
      throw new IllegalArgumentException("maxSize must be >= 1");
    }

    this.maxSize = maxSize;
    if (list.size() > maxSize) {
      throw new CollectionTooBigException(list.size(), maxSize);
    }
  }

  @Override
  public boolean append(Object e) {
    checkSize(size() + 1);
    return super.append(e);
  }

  @Override
  public void insert(int i, Object e) {
    checkSize(size() + 1);
    super.insert(i, e);
  }

  @Override
  public boolean add(Object element) {
    checkSize(size() + 1);
    return super.add(element);
  }

  @Override
  public void add(int index, Object element) {
    checkSize(size() + 1);
    super.add(index, element);
  }

  @Override
  public boolean addAll(int index, Collection<?> elements) {
    checkSize(size() + elements.size());
    return super.addAll(index, elements);
  }

  @Override
  public boolean addAll(Collection<?> elements) {
    checkSize(size() + elements.size());
    return super.addAll(elements);
  }

  @Override
  public PyList copy() {
    return new SizeLimitingPyList(new ArrayList<>(delegate()));
  }

  private void checkSize(int newSize) {
    if (newSize > maxSize) {
      throw new CollectionTooBigException(newSize, maxSize);
    }
  }
}
