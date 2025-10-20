package com.hubspot.jinjava.mode;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;

public class KeepUndefinedExecutionMode extends EagerExecutionMode {

  private static final ExecutionMode INSTANCE = new KeepUndefinedExecutionMode();

  protected KeepUndefinedExecutionMode() {}

  public static ExecutionMode instance() {
    return INSTANCE;
  }

  @Override
  public void prepareContext(Context context) {
    super.prepareContext(context);
    context.setDynamicVariableResolver(varName -> DeferredValue.instance());
  }
}
