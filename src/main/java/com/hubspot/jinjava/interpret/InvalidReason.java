package com.hubspot.jinjava.interpret;

public enum InvalidReason {
  NUMBER_FORMAT("with value '%s' must be a number"),
  NULL("cannot be null"),
  STRING("must be a string"),
  EXPRESSION_TEST("with value %s must be the name of an expression test"),
  TEMPORAL_UNIT("with value %s must be a valid temporal unit"),
  JSON_READ("could not be converted to an object"),
  JSON_WRITE("object could not be written as a string"),
  REGEX("with value %s must be valid regex")
  ;

  private final String errorMessage;

  InvalidReason(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
