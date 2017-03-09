package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.tree.parse.TagToken;

public class UnknownTagException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String tag;
  private final String defintion;

  public UnknownTagException(String tag, String definition, int lineNumber) {
    super(definition, "Unknown tag: " + tag, lineNumber);
    this.tag = tag;
    this.defintion = definition;
  }

  public UnknownTagException(TagToken tagToken) {
    this(tagToken.getTagName(), tagToken.getImage(), tagToken.getLineNumber());
  }

  public String getTag() {
    return tag;
  }

  public String getDefintion() {
    return defintion;
  }

}
