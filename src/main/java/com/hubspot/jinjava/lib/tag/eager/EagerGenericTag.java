package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.lib.tag.Tag;

@Beta
public class EagerGenericTag<T extends Tag> extends EagerTagDecorator<T> implements Tag {

  public EagerGenericTag(T tag) {
    super(tag);
  }
}
