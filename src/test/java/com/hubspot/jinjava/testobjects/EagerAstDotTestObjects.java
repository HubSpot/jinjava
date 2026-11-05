package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.PartiallyDeferredValue;

public class EagerAstDotTestObjects {

  public record RecordValue(String value) {}

  public record DeferredRecordValue(String value) implements PartiallyDeferredValue {
    @Override
    public String value() {
      if (value == null) {
        throw new DeferredValueException("record.value is deferred");
      }
      return value;
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }
  }

  public static class Foo implements PartiallyDeferredValue {

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
}
