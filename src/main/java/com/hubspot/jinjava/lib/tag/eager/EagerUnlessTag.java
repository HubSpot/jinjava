package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.lib.tag.UnlessTag;

@Beta
public class EagerUnlessTag extends EagerIfTag {

  public EagerUnlessTag() {
    super(new UnlessTag());
  }

  public EagerUnlessTag(UnlessTag unlessTag) {
    super(unlessTag);
  }
}
