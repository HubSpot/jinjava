package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.lib.tag.Tag;

public class EagerGenericTagDecorator<T extends Tag>
  extends EagerTagDecorator<T>
  implements Tag {

  public EagerGenericTagDecorator(T tag) {
    super(tag);
  }
}
