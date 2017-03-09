package com.hubspot.jinjava.interpret;

public class DisabledException extends InterpretException {

  private final String token;

  public DisabledException(String token) {
    super("'" + token + "' is disabled in this context");
    this.token = token;
  }

  public String getToken() {
    return token;
  }
}
