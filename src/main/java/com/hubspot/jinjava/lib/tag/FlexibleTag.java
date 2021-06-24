package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.tree.TagNode;

public interface FlexibleTag {
  boolean hasEndTag(TagNode tagNode);
}
