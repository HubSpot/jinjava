package com.hubspot.jinjava.interpret;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.function.Supplier;

public class LazyExpression implements Supplier {
  private final Supplier supplier;
  private final String image;
  private final Memoization memoization;
  private Object jsonValue = null;

  public enum Memoization {
    ON,
    OFF
  }

  protected LazyExpression(Supplier supplier, String image, Memoization memoization) {
    this.supplier = supplier;
    this.image = image;
    this.memoization = memoization;
  }

  public static LazyExpression of(Supplier supplier, String image) {
    return new LazyExpression(supplier, image, Memoization.ON);
  }

  public static LazyExpression of(
    Supplier supplier,
    String image,
    Memoization memoization
  ) {
    return new LazyExpression(supplier, image, memoization);
  }

  @Override
  public Object get() {
    if (jsonValue == null || memoization == Memoization.OFF) {
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
