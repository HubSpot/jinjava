package com.hubspot.jinjava.testobjects;

public enum OperationEnum {
  PLUS {
    @Override
    public int apply(int a, int b) {
      return a + b;
    }
  },
  TIMES {
    @Override
    public int apply(int a, int b) {
      return a * b;
    }
  };

  public abstract int apply(int a, int b);
}
