package com.hubspot.jinjava.interpret;

import java.util.function.Supplier;

public class LazyExpression implements Supplier {

  private final Supplier supplier;

  private LazyExpression(Supplier supplier) {
    this.supplier = supplier;
  }

  public static LazyExpression of(Supplier supplier) {
    return new LazyExpression(supplier);
  }

  @Override
  public Object get() {
    return supplier.get();
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
