package com.hubspot.jinjava.testobjects;

public class EagerExpressionResolverTestFoo {

  private final String bar;

  public EagerExpressionResolverTestFoo(String bar) {
    this.bar = bar;
  }

  public String bar() {
    return bar;
  }

  public String echo(String toEcho) {
    return toEcho;
  }
}
