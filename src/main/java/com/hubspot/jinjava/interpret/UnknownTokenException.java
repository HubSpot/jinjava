package com.hubspot.jinjava.interpret;

public class UnknownTokenException extends InterpretException {

  private static final long serialVersionUID = -388757722051666198L;
  private final String token;

  public UnknownTokenException(String token, int lineNumber, int startingPosition) {
    super("Unknown token found: " + token.trim(), lineNumber, startingPosition);
    this.token = token;
  }

  public String getToken() {
    return token;
  }
}
