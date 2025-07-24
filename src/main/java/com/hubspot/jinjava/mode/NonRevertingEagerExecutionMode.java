package com.hubspot.jinjava.mode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class NonRevertingEagerExecutionMode extends EagerExecutionMode {

  private static final ExecutionMode INSTANCE = new NonRevertingEagerExecutionMode();

  protected NonRevertingEagerExecutionMode() {}

  @SuppressFBWarnings(
    value = "HSM_HIDING_METHOD",
    justification = "Purposefully overriding to return static instance of this class."
  )
  public static ExecutionMode instance() {
    return INSTANCE;
  }

  @Override
  public boolean useEagerContextReverting() {
    return false;
  }
}
