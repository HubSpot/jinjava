package com.hubspot.jinjava.interpret;

import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonValue;

public class LazyExpression implements Supplier {
  private final Supplier supplier;
  private final String image;
  private Object jsonValue = null;

  private LazyExpression(Supplier supplier, String image) {
    this.supplier = supplier;
    this.image = image;
  }

  public static LazyExpression of(Supplier supplier, String image) {
    return new LazyExpression(supplier, image);
  }

  @Override
  public Object get() {
    if (jsonValue == null) {
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
