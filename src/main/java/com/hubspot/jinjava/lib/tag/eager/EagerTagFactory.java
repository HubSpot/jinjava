package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.base.Throwables;
import com.hubspot.jinjava.lib.tag.Tag;

public class EagerTagFactory {

  public <T extends Tag> EagerTag getEagerTag(Class<T> clazz) {
    try {
      T tag = clazz.getDeclaredConstructor().newInstance();
      return new EagerTagDecorator<>(tag);
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }
}
