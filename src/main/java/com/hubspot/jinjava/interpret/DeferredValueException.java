package com.hubspot.jinjava.interpret;

/**
 * Exception thrown when attempting to render a {@link com.hubspot.jinjava.interpret.DeferredValue}.
 * The exception is effectively used for flow control, to unwind evaluating a template Node
 * and instead echo its contents to the output.
 */
public class DeferredValueException extends InterpretException {

  public static final String MESSAGE_PREFIX = "Encountered a deferred value: ";

  public DeferredValueException(String message) {
    super(message);
  }

  public DeferredValueException(String variable, int lineNumber, int startPosition) {
    super(MESSAGE_PREFIX + '\"' + variable + '\"', lineNumber, startPosition);
  }
}
