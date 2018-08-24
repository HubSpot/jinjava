package com.hubspot.jinjava.objects.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.collect.Iterators;

public class LazyList<T> implements List<T> {

  private ArrayWrappedIterator<T> iterator;
  private ArrayList<T> list;

  public LazyList(Iterator<T> iterator) {
    this.list = new ArrayList<>();
    this.iterator = new ArrayWrappedIterator<>(iterator, this.list);
  }

  @Override
  public int size() {
    iterator.unpack();
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return !iterator.hasNext() && list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {

    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).equals(o)) {
        return true;
      }
    }

    while (iterator.hasNext()) {
      if (iterator.next().equals(o)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Iterator<T> iterator() {
    return Iterators.concat(list.iterator(), iterator);
  }

  @Override
  public Object[] toArray() {
    iterator.unpack();
    return list.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    iterator.unpack();
    return list.toArray(a);
  }

  @Override
  public boolean add(T t) {
    iterator = new ArrayWrappedIterator<>(Iterators.concat(iterator.getIterator(), Collections.singleton(t).iterator()), list);
    return true;
  }

  @Override
  public boolean remove(Object o) {

    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).equals(o)) {
        list.remove(i);
        return true;
      }
    }

    while (iterator.hasNext()) {
      if (iterator.next().equals(o)) {
        list.remove(list.size() - 1);
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    Set<?> set = new HashSet<>(c);


    for (int i = 0; i < list.size(); i++) {
      T object = list.get(i);
      if (set.contains(object)) {
        set.remove(object);
      }
      if (set.isEmpty()) {
        return true;
      }
    }

    while (iterator.hasNext()) {
      T object = iterator.next();
      if (set.contains(object)) {
        set.remove(object);
      }
      if (set.isEmpty()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    iterator = new ArrayWrappedIterator<>(Iterators.concat(iterator.getIterator(), c.iterator()), list);
    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {

    int offset = index - list.size();
    if (offset < 0) {
      list.addAll(index, c);
    } else if (offset == 0) {
      list.addAll(c);
    } else {
      while (offset != 0) {
        if (!iterator.hasNext()) {
          throw new IndexOutOfBoundsException();
        }
        iterator.next();
        offset--;
      }
      list.addAll(c);
    }
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    iterator.unpack();
    return list.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    iterator.unpack();
    return list.retainAll(c);
  }

  @Override
  public void clear() {
    list.clear();
    iterator = new ArrayWrappedIterator<>(Collections.emptyIterator(), list);
  }

  @Override
  public T get(int index) {

    int offset = index - list.size();
    if (offset <= 0) {
      return list.get(index);
    } else {
      while (offset != 0) {
        if (!iterator.hasNext()) {
          throw new IndexOutOfBoundsException();
        }
        iterator.next();
        offset--;
      }
       return list.get(index);
    }
  }

  @Override
  public T set(int index, T element) {

    int offset = index - list.size();
    if (offset <= 0) {
      return list.set(index, element);
    } else {
      while (offset != 0) {
        if (!iterator.hasNext()) {
          throw new IndexOutOfBoundsException();
        }
        iterator.next();
        offset--;
      }
      return list.set(index, element);
    }
  }

  @Override
  public void add(int index, T element) {

    int offset = index - list.size();
    if (offset <= 0) {
      list.add(index, element);
    } else {
      while (offset != 0) {
        if (!iterator.hasNext()) {
          throw new IndexOutOfBoundsException();
        }
        iterator.next();
        offset--;
      }
      list.add(index, element);
    }
  }

  @Override
  public T remove(int index) {

    int offset = index - list.size();
    if (offset <= 0) {
      return list.remove(index);
    } else {
      while (offset != 0) {
        if (!iterator.hasNext()) {
          throw new IndexOutOfBoundsException();
        }
        iterator.next();
        offset--;
      }
      return list.remove(index);
    }
  }

  @Override
  public int indexOf(Object o) {

    int i = 0;
    for (; i < list.size(); i++) {
      if (list.get(i).equals(o)) {
        return i;
      }
    }

    while (iterator.hasNext()) {
      i++;
      if (iterator.next().equals(o)) {
        return i;
      }
    }

    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    iterator.unpack();
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    iterator.unpack();
    return list.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    iterator.unpack();
    return list.listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {

    int offset = toIndex - list.size();
    if (offset <= 0) {
      return list.subList(fromIndex, toIndex);
    } else {
      while (offset != 0) {
        if (!iterator.hasNext()) {
          throw new IndexOutOfBoundsException();
        }
        iterator.next();
        offset--;
      }
      return list.subList(fromIndex, toIndex);
    }
  }

  @Override
  public String toString() {
    iterator.unpack();
    StringBuilder sb = new StringBuilder().append('[');
    boolean first = true;
    for (Object o : list) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      if (o == list) {
        sb.append("(this Collection)");
      } else {
        sb.append(o.toString());
      }
    }
    return sb.append(']').toString();
  }
}
