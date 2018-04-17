package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.tree.parse.TagToken;

public class UnknownTagException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String tag;
  private final String definition;

  public UnknownTagException(String tag, String definition, int lineNumber, int startPosition) {
    super(definition, "Unknown tag: " + tag, lineNumber, startPosition);
    this.tag = tag;
    this.definition = definition;
  }

  public UnknownTagException(TagToken tagToken) {
    this(tagToken.getRawTagName(), tagToken.getImage(), tagToken.getLineNumber(), tagToken.getStartPosition());
  }

  public String getTag() {
    return tag;
  }

  /**
   * @deprecated use correct spelling
   */
  @Deprecated
  public String getDefintion() {
    return definition;
  }

  public String getDefinition() {
    return definition;
  }

}
