package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.base.Throwables;
import com.hubspot.jinjava.lib.tag.Tag;

public class EagerTagFactory {

  public <T extends Tag> EagerTagDecorator<T> getEagerTagDecorator(Class<T> clazz) {
    try {
      T tag = clazz.getDeclaredConstructor().newInstance();
      return new EagerGenericTagDecorator<>(tag);
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }
}
