package com.hubspot.jinjava.interpret;

import org.apache.commons.lang3.StringUtils;

public class MissingEndTagException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String endTag;
  private final String startDefinition;

  public MissingEndTagException(String endTag, String startDefintion, int lineNumber, int startPosition) {
    super(startDefintion, "Missing end tag: " + endTag + " for tag defined as: " + StringUtils.abbreviate(startDefintion, 255), lineNumber, startPosition);
    this.endTag = endTag;
    this.startDefinition = startDefintion;
  }

  public String getEndTag() {
    return endTag;
  }

  public String getStartDefinition() {
    return startDefinition;
  }

}
