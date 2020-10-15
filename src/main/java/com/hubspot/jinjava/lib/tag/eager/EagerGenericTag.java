package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.lib.tag.Tag;

public class EagerGenericTag<T extends Tag> extends EagerTagDecorator<T> implements Tag {

  public EagerGenericTag(T tag) {
    super(tag);
  }
}
