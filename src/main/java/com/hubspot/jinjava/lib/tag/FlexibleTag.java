package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.tree.parse.TagToken;

public interface FlexibleTag {
  boolean hasEndTag(TagToken tagToken);
}
