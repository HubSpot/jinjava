package com.hubspot.jinjava.mode;

import com.hubspot.jinjava.interpret.Context;

public interface ExecutionMode {
  default boolean isPreserveRawTags() {
    return false;
  }

  default boolean useEagerParser() {
    return false;
  }

  default boolean useEagerContextReverting() {
    return false;
  }

  default void prepareContext(Context context) {}
}
