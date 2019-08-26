package com.hubspot.jinjava.interpret;

import java.util.function.Supplier;

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
  public Object get() {
    return supplier.get();
  }

  public String getImage() {
    return image;
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
