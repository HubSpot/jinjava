package com.hubspot.jinjava.objects.collections;

import com.google.common.collect.ForwardingSet;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.Objects;
import java.util.Set;

public class PySet extends ForwardingSet<Object> implements PyWrapper {

  private boolean computingHashCode = false;
  private final Set<Object> set;

  public PySet(Set<Object> set) {
    this.set = set;
  }

  @Override
  protected Set<Object> delegate() {
    return set;
  }

  /**
   * This is not thread-safe
   * @return hashCode, preventing recursion
   */
  @Override
  public int hashCode() {
    if (computingHashCode) {
      return Objects.hashCode(null);
    }
    try {
      computingHashCode = true;
      return super.hashCode();
    } finally {
      computingHashCode = false;
    }
  }
}
