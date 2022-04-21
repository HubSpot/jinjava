package com.hubspot.jinjava.mode;

import com.hubspot.jinjava.interpret.Context;

public interface ExecutionMode {
  default boolean isPreserveRawTags() {
    return false;
  }

  default boolean useEagerParser() {
    return false;
  }

  /**
   * This will determine if the entire context can be reverted or if only the current scope can get reverted.
   * A snapshot of the context is created so it is expensive to do so with the entire context, but less expensive
   * to only do that with the current scope
   * @return whether the entire context (true) or just the current scope (false) will have a snapshot created to
   * allow reverting of modified values in deferred execution mode.
   */
  default boolean useEagerContextReverting() {
    return false;
  }

  default void prepareContext(Context context) {}
}
