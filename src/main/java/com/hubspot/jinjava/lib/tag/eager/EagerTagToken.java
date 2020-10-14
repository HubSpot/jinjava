package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.Set;

public class EagerTagToken {
  private TagToken tagToken;
  private Set<String> deferredHelpers;

  public EagerTagToken(TagToken tagToken, Set<String> deferredHelpers) {
    this.tagToken = tagToken;
    this.deferredHelpers = deferredHelpers;
  }

  public TagToken getTagToken() {
    return tagToken;
  }

  public Set<String> getDeferredHelpers() {
    return deferredHelpers;
  }
}
