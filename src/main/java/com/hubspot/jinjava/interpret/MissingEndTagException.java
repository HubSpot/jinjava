package com.hubspot.jinjava.interpret;

public class MissingEndTagException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String endTag;
  private final String startDefinition;
  
  public MissingEndTagException(String endTag, String startDefintion, int lineNumber) {
    super(startDefintion, "Missing end tag: " + endTag + "for tag defined as: " + startDefintion, lineNumber);
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
