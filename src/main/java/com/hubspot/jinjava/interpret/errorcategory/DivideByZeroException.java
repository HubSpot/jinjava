package com.hubspot.jinjava.interpret.errorcategory;

public class DivideByZeroException extends RuntimeException {
  private final String errorMessage;

  public DivideByZeroException(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getMessage() {
    return errorMessage;
  }
}
