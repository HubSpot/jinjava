package com.hubspot.jinjava.interpret;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.function.Supplier;

public class LazyExpression implements Supplier {
  private final Supplier supplier;
  private final String image;
  private final boolean memoize;
  private Object jsonValue = null;

  private LazyExpression(Supplier supplier, String image, boolean memoize) {
    this.supplier = supplier;
    this.image = image;
    this.memoize = memoize;
  }

  public static LazyExpression of(Supplier supplier, String image) {
    return new LazyExpression(supplier, image, true);
  }

  public static LazyExpression of(Supplier supplier, String image, boolean memoize) {
    return new LazyExpression(supplier, image, memoize);
  }

  @Override
  public Object get() {
    if (jsonValue == null || !memoize) {
      jsonValue = supplier.get();
    }
    return jsonValue;
  }

  @Override
  public String toString() {
    return image;
  }

  @JsonValue
  public Object getJsonValue() {
    return jsonValue == null ? "" : jsonValue;
  }
}
