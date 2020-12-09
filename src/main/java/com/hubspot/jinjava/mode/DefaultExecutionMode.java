package com.hubspot.jinjava.mode;

public class DefaultExecutionMode implements ExecutionMode {
  private static final ExecutionMode INSTANCE = new DefaultExecutionMode();

  private DefaultExecutionMode() {}

  public static ExecutionMode instance() {
    return INSTANCE;
  }
}
