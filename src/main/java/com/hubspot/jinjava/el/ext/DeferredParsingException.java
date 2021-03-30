package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.interpret.DeferredValueException;

public class DeferredParsingException extends DeferredValueException {
  private final String deferredEvalResult;

  public DeferredParsingException(Class<?> clazz, String deferredEvalResult) {
    super(
      String.format(
        "%s could not be parsed more than: %s",
        clazz.toString(),
        deferredEvalResult
      )
    );
    this.deferredEvalResult = deferredEvalResult;
  }

  public String getDeferredEvalResult() {
    return deferredEvalResult;
  }
}
