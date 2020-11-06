package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.interpret.DeferredValueException;

public class DeferredParsingException extends DeferredValueException {
  private final String deferredEvalResult;

  public DeferredParsingException(String deferredEvalResult) {
    super("AstNode could not be parsed more than: " + deferredEvalResult);
    this.deferredEvalResult = deferredEvalResult;
  }

  public String getDeferredEvalResult() {
    return deferredEvalResult;
  }
}
