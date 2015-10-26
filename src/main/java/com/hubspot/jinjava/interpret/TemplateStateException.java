package com.hubspot.jinjava.interpret;

public class TemplateStateException extends InterpretException {
  private static final long serialVersionUID = 426925445445430522L;

  public TemplateStateException(String msg) {
    super(msg);
  }

  public TemplateStateException(String msg, Throwable e) {
    super(msg, e);
  }

  public TemplateStateException(String msg, int lineNumber) {
    super(msg, lineNumber);
  }

  public TemplateStateException(String msg, Throwable e, int lineNumber) {
    super(msg, e, lineNumber);
  }

}
