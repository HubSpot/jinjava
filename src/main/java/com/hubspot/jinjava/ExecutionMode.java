package com.hubspot.jinjava;

public enum ExecutionMode {
  DEFAULT,
  PRE_RENDER(true, false),
  EAGER_PRE_RENDER(true, true);

  private final boolean preserveForFinalPass;
  private final boolean eagerExecutionEnabled;

  ExecutionMode() {
    this.preserveForFinalPass = false;
    this.eagerExecutionEnabled = false;
  }

  ExecutionMode(boolean preserveForFinalPass, boolean eagerExecutionEnabled) {
    this.preserveForFinalPass = preserveForFinalPass;
    this.eagerExecutionEnabled = eagerExecutionEnabled;
  }

  public boolean isPreserveForFinalPass() {
    return preserveForFinalPass;
  }

  public boolean isEagerExecutionEnabled() {
    return eagerExecutionEnabled;
  }
}
