package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.tree.parse.TagToken;

public class UnknownTagException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String tag;
  private final String defintion;

  public UnknownTagException(String tag, String definition, int lineNumber, int startPosition) {
    super(definition, "Unknown tag: " + tag, lineNumber, startPosition);
    this.tag = tag;
    this.defintion = definition;
  }

  public UnknownTagException(TagToken tagToken) {
    this(tagToken.getTagName(), tagToken.getImage(), tagToken.getLineNumber(), tagToken.getStartPosition());
  }

  public String getTag() {
    return tag;
  }

  public String getDefintion() {
    return defintion;
  }

}
