package com.hubspot.jinjava.interpret;

public enum InvalidReason {
  NUMBER_FORMAT("with value '%s' must be a number"),
  NULL("cannot be null"),
  STRING("must be a string"),
  EXPRESSION_TEST("with value %s must be the name of an expression test"),
  TEMPORAL_UNIT("with value %s must be a valid temporal unit"),
  JSON_READ("could not be converted to an object"),
  JSON_WRITE("object could not be written as a string"),
  REGEX("with value %s must be valid regex"),
  POSITIVE_NUMBER("with value %s must be a positive number"),
  NULL_IN_LIST("of type 'list' cannot contain a null item"),
  NULL_ATTRIBUTE_IN_LIST("with value '%s' must be a valid attribute of every item in the list"),
  ENUM("with value '%s' must be one of: %s"),
  CIDR("with value '%s' must be a valid CIDR address");

  private final String errorMessage;

  InvalidReason(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
