package com.hubspot.jinjava.objects.serialization;

import com.hubspot.jinjava.objects.PyWrapper;

public interface PyishSerializable extends PyWrapper {
  /**
   * Allows for a class to specify a custom string representation in Jinjava.
   * By default, this will refer to the <code>toString()</code> method,
   * but this method can be overriden to provide a representation separate from the
   * normal <code>toString()</code> result.
   * @return A pythonic/json string representation of the object
   */
  default String toPyishString() {
    return '"' + toString() + '"';
  }
}
