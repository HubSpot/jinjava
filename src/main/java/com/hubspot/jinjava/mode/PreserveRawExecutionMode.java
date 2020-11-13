package com.hubspot.jinjava.mode;

public class PreserveRawExecutionMode implements ExecutionMode {

  @Override
  public boolean isPreserveRawTags() {
    return true;
  }
}
