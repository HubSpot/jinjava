package com.hubspot.jinjava.el.ext;

public class DeferredInvocationResolutionException extends DeferredParsingException {

  public DeferredInvocationResolutionException(String invocationResultString) {
    super(DeferredInvocationResolutionException.class, invocationResultString);
  }
}
