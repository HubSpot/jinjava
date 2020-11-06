package com.hubspot.jinjava.el.ext;

public class DeferredParsingException extends RuntimeException {
  private final String deferredEvalResult;

  public DeferredParsingException(String deferredEvalResult) {
    super("AstNode could not be parsed more than: " + deferredEvalResult);
    this.deferredEvalResult = deferredEvalResult;
  }

  public String getDeferredEvalResult() {
    return deferredEvalResult;
  }
}
