package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.tree.parse.TagToken;

public class UnknownTagException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String tag;
  private final String defintion;

  public UnknownTagException(String tag, String defintion, int lineNumber) {
    super(defintion, "Unknown tag: " + tag, lineNumber);
    this.tag = tag;
    this.defintion = defintion;
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
