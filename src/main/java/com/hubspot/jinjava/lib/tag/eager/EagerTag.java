package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;

public interface EagerTag extends Tag {
  String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter);
}
