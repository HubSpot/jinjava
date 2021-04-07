package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.interpret.DeferredValueException;

public class DeferredParsingException extends DeferredValueException {
  private final String deferredEvalResult;
  private final Object sourceNode;

  public DeferredParsingException(Object sourceNode, String deferredEvalResult) {
    super(
      String.format(
        "%s could not be parsed more than: %s",
        sourceNode.getClass(),
        deferredEvalResult
      )
    );
    this.deferredEvalResult = deferredEvalResult;
    this.sourceNode = sourceNode;
  }

  public String getDeferredEvalResult() {
    return deferredEvalResult;
  }

  public Object getSourceNode() {
    return sourceNode;
  }
}
