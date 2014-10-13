package com.hubspot.jinjava.objects.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.hubspot.jinjava.objects.PyWrapper;

public class PyList implements List<Object>, PyWrapper {

  private List<Object> list;
  
  public PyList(List<Object> list) {
    this.list = list;
  }

  public List<Object> toList() {
    return list;
  }
  
  @Override
  public String toString() {
    return list.toString();
  }
  
  public int size() {
    return list.size();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public boolean contains(Object o) {
    return list.contains(o);
  }

  public Iterator<Object> iterator() {
    return list.iterator();
  }

  public Object[] toArray() {
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  public boolean add(Object e) {
    return list.add(e);
  }

  public boolean append(Object e) {
    return add(e);
  }
  
  public boolean remove(Object o) {
    return list.remove(o);
  }

  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  public boolean addAll(Collection<? extends Object> c) {
    return list.addAll(c);
  }

  public boolean addAll(int index, Collection<? extends Object> c) {
    return list.addAll(index, c);
  }

  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  public void clear() {
    list.clear();
  }

  public boolean equals(Object o) {
    return list.equals(o);
  }

  public int hashCode() {
    return list.hashCode();
  }

  public Object get(int index) {
    return list.get(index);
  }

  public Object set(int index, Object element) {
    return list.set(index, element);
  }

  public void add(int index, Object element) {
    list.add(index, element);
  }

  public Object remove(int index) {
    return list.remove(index);
  }

  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  public ListIterator<Object> listIterator() {
    return list.listIterator();
  }

  public ListIterator<Object> listIterator(int index) {
    return list.listIterator(index);
  }

  public List<Object> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }
  
}
