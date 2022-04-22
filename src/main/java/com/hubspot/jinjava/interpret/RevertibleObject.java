package com.hubspot.jinjava.interpret;

public class RevertibleObject {
  private final Object hashCode;
  private final String pyishString;

  public RevertibleObject(Object hashCode, String pyishString) {
    this.hashCode = hashCode;
    this.pyishString = pyishString;
  }

  public Object getHashCode() {
    return hashCode;
  }

  public String getPyishString() {
    return pyishString;
  }
}
