package com.hubspot.jinjava.interpret;

public class UnknownTagException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String tag;
  private final String defintion;
  
  public UnknownTagException(String tag, String defintion, int lineNumber) {
    super(defintion, "Unknown tag: " + tag, lineNumber);
    this.tag = tag;
    this.defintion = defintion;
  }

  public String getTag() {
    return tag;
  }
  
  public String getDefintion() {
    return defintion;
  }

}
