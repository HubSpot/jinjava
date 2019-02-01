package com.hubspot.jinjava.interpret.errorcategory;

import com.hubspot.jinjava.interpret.InterpretException;

public class DeferredValueEncounteredException extends InterpretException {
  public DeferredValueEncounteredException(String variable, int lineNumber, int startPosition) {
    super("Encountered a deferred value: \"" + variable + "\"", lineNumber, startPosition);
  }
}
