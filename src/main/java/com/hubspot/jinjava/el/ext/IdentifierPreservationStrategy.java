package com.hubspot.jinjava.el.ext;

public enum IdentifierPreservationStrategy {
  PRESERVING(true),
  RESOLVING(false);

  public static IdentifierPreservationStrategy preserving(boolean preserveIdentifier) {
    return preserveIdentifier ? PRESERVING : RESOLVING;
  }

  private final boolean preserving;

  IdentifierPreservationStrategy(boolean preserving) {
    this.preserving = preserving;
  }

  public boolean isPreserving() {
    return preserving;
  }
}
