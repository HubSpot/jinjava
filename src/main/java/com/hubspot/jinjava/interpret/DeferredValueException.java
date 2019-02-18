package com.hubspot.jinjava.interpret;

/**
 * Exception thrown when attempting to render a DeferredValue.
 * The exception is effectively used for flow control, to unwind evaluating a template Node
 * and instead echo its contents to the output.
 */
public class DeferredValueException extends InterpretException {
  public DeferredValueException(String variable, int lineNumber, int startPosition) {
    super("Encountered a deferred value: \"" + variable + "\"", lineNumber, startPosition);
  }
}
