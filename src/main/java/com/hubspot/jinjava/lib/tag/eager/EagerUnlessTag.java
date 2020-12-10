package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.lib.tag.UnlessTag;

public class EagerUnlessTag extends EagerIfTag {

  public EagerUnlessTag() {
    super(new UnlessTag());
  }

  public EagerUnlessTag(UnlessTag unlessTag) {
    super(unlessTag);
  }
}
