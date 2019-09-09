package com.hubspot.jinjava.interpret;

import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonValue;

public class LazyExpression implements Supplier {

  private final Supplier supplier;
  private final String image;

  private LazyExpression(Supplier supplier, String image) {
    this.supplier = supplier;
    this.image = image;
  }

  public static LazyExpression of(Supplier supplier, String image) {
    return new LazyExpression(supplier, image);
  }

  @Override
  @JsonValue
  public Object get() {
    return supplier.get();
  }

  @Override
  public String toString() {
    return image;
  }
}
