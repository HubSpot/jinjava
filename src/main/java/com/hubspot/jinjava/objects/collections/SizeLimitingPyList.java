package com.hubspot.jinjava.objects.collections;

import com.hubspot.jinjava.interpret.IndexOutOfRangeException;
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
    if (list.size() >= maxSize) {
      throw createOutOfRangeException(list.size());
    }
  }

  @Override
  public boolean append(Object e) {
    if (size() >= maxSize) {
      throw createOutOfRangeException(size() + 1);
    }
    return super.append(e);
  }

  @Override
  public void insert(int i, Object e) {
    if (size() >= maxSize) {
      throw createOutOfRangeException(size() + 1);
    }
    super.insert(i, e);
  }

  @Override
  public boolean add(Object element) {
    if (size() >= maxSize) {
      throw createOutOfRangeException(size() + 1);
    }
    return super.add(element);
  }

  @Override
  public void add(int index, Object element) {
    if (size() >= maxSize) {
      throw createOutOfRangeException(size() + 1);
    }
    super.add(index, element);
  }

  @Override
  public boolean addAll(int index, Collection<?> elements) {
    if (size() + elements.size() >= maxSize) {
      throw createOutOfRangeException(size() + elements.size());
    }
    return super.addAll(index, elements);
  }

  @Override
  public boolean addAll(Collection<?> collection) {
    if (size() + collection.size() >= maxSize) {
      throw createOutOfRangeException(size() + collection.size());
    }
    return super.addAll(collection);
  }

  @Override
  public PyList copy() {
    return new SizeLimitingPyList(new ArrayList<>(delegate()));
  }

  IndexOutOfRangeException createOutOfRangeException(int index) {
    return new IndexOutOfRangeException(
      String.format(
        "Index %d is out of range for list of maximum size %d",
        index,
        maxSize
      )
    );
  }
}
