package com.hubspot.jinjava.mode;

public class NonRevertingEagerExecutionMode extends EagerExecutionMode {

  private static final ExecutionMode INSTANCE = new NonRevertingEagerExecutionMode();

  protected NonRevertingEagerExecutionMode() {}

  public static ExecutionMode instance() {
    return INSTANCE;
  }

  @Override
  public boolean useEagerContextReverting() {
    return false;
  }
}
