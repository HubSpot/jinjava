package com.hubspot.jinjava.interpret;

public enum TemplateErrorReason implements ErrorReason {
  SYNTAX_ERROR,
  UNKNOWN,
  BAD_URL,
  EXCEPTION,
  MISSING,
  DISABLED,
  OTHER
}
