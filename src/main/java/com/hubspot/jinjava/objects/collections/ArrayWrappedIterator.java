package com.hubspot.jinjava.objects.collections;

import java.util.Iterator;
import java.util.List;

public class ArrayWrappedIterator<T> implements Iterator<T> {

  private final Iterator<T> iterator;
  private final List<T> list;

  public ArrayWrappedIterator(Iterator<T> iterator, List<T> list) {
    this.iterator = iterator;
    this.list = list;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public T next() {
    if (!hasNext()) {
      throw new ArrayIndexOutOfBoundsException();
    }
    T next = iterator.next();
    list.add(next);
    return next;
  }

  public void unpack() {
    while (hasNext()) {
      next();
    }
  }

  public Iterator<T> getIterator() {
    return iterator;
  }
}
