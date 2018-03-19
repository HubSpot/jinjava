package com.hubspot.jinjava.interpret;

public class TemplateStateException extends InterpretException {
  private static final long serialVersionUID = 426925445445430522L;

  public TemplateStateException(String msg) {
    super(msg);
  }

  public TemplateStateException(String msg, Throwable e) {
    super(msg, e);
  }

  public TemplateStateException(String msg, int lineNumber, int startPosition) {
    super(msg, lineNumber, startPosition);
  }

  public TemplateStateException(String msg, int lineNumber) {
    super(msg, lineNumber, -1);
  }

  public TemplateStateException(String msg, Throwable e, int lineNumber, int startPosition) {
    super(msg, e, lineNumber, startPosition);
  }

  public TemplateStateException(String msg, Throwable e, int lineNumber) {
    super(msg, e, lineNumber, -1);
  }

}
