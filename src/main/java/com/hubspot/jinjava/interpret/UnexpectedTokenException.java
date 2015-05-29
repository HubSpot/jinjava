package com.hubspot.jinjava.interpret;

public class UnexpectedTokenException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String token;

  public UnexpectedTokenException(String token, int lineNumber) {
    super(token, "Unexpected token: " + token, lineNumber);
    this.token = token;
  }

  public String getToken() {
    return token;
  }

}
