package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;

public final class EndTag implements Tag {

  private final String endTagName;
  
  public EndTag(Tag tag) {
    this.endTagName = tag.getEndTagName();
  }
  
  @Override
  public String getName() {
    return endTagName;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return "";
  }

  @Override
  public String getEndTagName() {
    return endTagName;
  }

}
