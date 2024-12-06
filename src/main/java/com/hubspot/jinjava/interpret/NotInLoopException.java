package com.hubspot.jinjava.interpret;

/**
 * Exception thrown when `continue` or `break` is called outside of a loop
 */
public class NotInLoopException extends InterpretException {

  public static final String MESSAGE_PREFIX = "`";
  public static final String MESSAGE_SUFFIX = "` called while not in a for loop";

  public NotInLoopException(String tagName) {
    super(MESSAGE_PREFIX + tagName + MESSAGE_SUFFIX);
  }
}
