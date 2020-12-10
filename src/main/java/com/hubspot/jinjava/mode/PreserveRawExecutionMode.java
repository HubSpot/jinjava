package com.hubspot.jinjava.mode;

public class PreserveRawExecutionMode implements ExecutionMode {
  private static final ExecutionMode INSTANCE = new PreserveRawExecutionMode();

  private PreserveRawExecutionMode() {}

  public static ExecutionMode instance() {
    return INSTANCE;
  }

  @Override
  public boolean isPreserveRawTags() {
    return true;
  }
}
