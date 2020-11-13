package com.hubspot.jinjava.mode;

import com.hubspot.jinjava.interpret.Context;

public class EagerExecutionMode implements ExecutionMode {

  @Override
  public boolean isPreserveRawTags() {
    return true;
  }

  @Override
  public void prepareContext(Context context) {
    // TODO register eager tags & expression node
  }
}
