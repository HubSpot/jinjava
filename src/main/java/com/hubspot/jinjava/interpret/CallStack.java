package com.hubspot.jinjava.interpret;

import java.util.Optional;
import java.util.Stack;

public class CallStack {

  private final CallStack parent;
  private final Class<? extends TagCycleException> exceptionClass;
  private final Stack<String> stack = new Stack<>();

  public CallStack(CallStack parent, Class<? extends TagCycleException> exceptionClass) {
    this.parent = parent;
    this.exceptionClass = exceptionClass;
  }

  public boolean contains(String path) {
    if (stack.contains(path)) {
      return true;
    }

    if (parent != null) {
      return parent.contains(path);
    }

    return false;
  }

  /**
   * This is added to allow for recursive macro calls. Adds the given path to the
   * call stack without checking for a cycle.
   * @param path the path to be added.
   */
  public void pushWithoutCycleCheck(String path) {
    stack.push(path);
  }

  public void push(String path, int lineNumber, int startPosition) {
    if (contains(path)) {
      throw TagCycleException.create(exceptionClass, path, Optional.of(lineNumber), Optional.of(startPosition));
    }

    stack.push(path);
  }

  public Optional<String> pop() {
    if (stack.isEmpty()) {
      if (parent != null) {
        return parent.pop();
      }
      return Optional.empty();
    }

    return Optional.of(stack.pop());
  }
}
