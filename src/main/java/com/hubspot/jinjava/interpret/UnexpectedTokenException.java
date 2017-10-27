package com.hubspot.jinjava.interpret;

public class UnexpectedTokenException extends TemplateSyntaxException {
  private static final long serialVersionUID = 1L;

  private final String token;

  public UnexpectedTokenException(String token, int lineNumber, int startPosition) {
    super(token, "Unexpected token: " + token, lineNumber, startPosition);
    this.token = token;
  }

  public String getToken() {
    return token;
  }

}
