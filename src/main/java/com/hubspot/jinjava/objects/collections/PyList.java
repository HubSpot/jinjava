package com.hubspot.jinjava.objects.collections;

import java.util.List;

import com.google.common.collect.ForwardingList;
import com.hubspot.jinjava.objects.PyWrapper;

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

}
