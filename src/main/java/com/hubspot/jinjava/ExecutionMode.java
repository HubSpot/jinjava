package com.hubspot.jinjava;

public enum ExecutionMode {
  DEFAULT,
  PRESERVE_RAW(true, false),
  EAGER_EXECUTION(true, true);

  private final boolean preserveRawTags;
  private final boolean eagerExecutionEnabled;

  ExecutionMode() {
    this.preserveRawTags = false;
    this.eagerExecutionEnabled = false;
  }

  ExecutionMode(boolean preserveRawTags, boolean eagerExecutionEnabled) {
    this.preserveRawTags = preserveRawTags;
    this.eagerExecutionEnabled = eagerExecutionEnabled;
  }

  public boolean isPreserveRawTags() {
    return preserveRawTags;
  }

  public boolean isEagerExecutionEnabled() {
    return eagerExecutionEnabled;
  }
}
