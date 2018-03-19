package com.hubspot.jinjava.interpret;

public class UnknownTokenException extends InterpretException {

  private static final long serialVersionUID = -388757722051666198L;
  private final String token;

  public UnknownTokenException(String token, int lineNumber, int startPosition) {
    super("Unknown token found: " + token, lineNumber, startPosition);
    this.token = token;
  }

  public String getToken() {
    return token;
  }
}
