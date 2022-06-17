package com.hubspot.jinjava.interpret;

import java.util.Optional;

public class RevertibleObject {
  private final Object hashCode;
  private final Optional<String> pyishString;

  public RevertibleObject(Object hashCode) {
    this.hashCode = hashCode;
    pyishString = Optional.empty();
  }

  public RevertibleObject(Object hashCode, String pyishString) {
    this.hashCode = hashCode;
    this.pyishString = Optional.ofNullable(pyishString);
  }

  public Object getHashCode() {
    return hashCode;
  }

  public Optional<String> getPyishString() {
    return pyishString;
  }
}
