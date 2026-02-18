package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.PartiallyDeferredValue;

public class EagerAstDotTestObject implements PartiallyDeferredValue {

  public String getDeferred() {
    throw new DeferredValueException("foo.deferred is deferred");
  }

  public String getResolved() {
    return "resolved";
  }

  @Override
  public Object getOriginalValue() {
    return null;
  }
}
