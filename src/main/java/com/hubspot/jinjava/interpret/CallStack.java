package com.hubspot.jinjava.interpret;

import java.util.Optional;
import java.util.Stack;

public class CallStack {

  private final CallStack parent;
  private final Class<? extends TagCycleException> exceptionClass;
  private final Stack<String> stack = new Stack<>();
  private final int depth;
  private int topLineNumber;
  private int topStartPosition;
  private boolean inheritFromParent;

  public CallStack(CallStack parent, Class<? extends TagCycleException> exceptionClass) {
    this.parent = parent;
    this.exceptionClass = exceptionClass;

    this.depth = parent == null ? 0 : parent.depth + 1;
    if (parent != null) {
      if (!parent.isEmpty()) {
        stack.push(parent.peek().get());
      }
      if (parent.inheritFromParent || parent.topLineNumber > 0) {
        this.inheritFromParent = true;
        this.topStartPosition = parent.topStartPosition;
        this.topLineNumber = parent.topLineNumber;
      }
    }
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
  public void pushWithoutCycleCheck(String path, int lineNumber, int startPosition) {
    pushToStack(path, lineNumber, startPosition);
  }

  public void pushWithMaxDepth(String path, int maxDepth, int lineNumber, int startPosition) {
    if (depth < maxDepth) {
      pushToStack(path, lineNumber, startPosition);
    } else {
      throw TagCycleException.create(exceptionClass, path, Optional.of(lineNumber), Optional.of(startPosition));
    }
  }

  public void push(String path, int lineNumber, int startPosition) {
    if (contains(path)) {
      throw TagCycleException.create(exceptionClass, path, Optional.of(lineNumber), Optional.of(startPosition));
    }

    pushToStack(path, lineNumber, startPosition);
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

  public Optional<String> peek() {
    if (stack.isEmpty()) {
      if (parent != null) {
        return parent.peek();
      }
      return Optional.empty();
    }

    return Optional.of(stack.peek());
  }

  public int getTopLineNumber() {
    return topLineNumber;
  }

  public int getTopStartPosition() {
    return topStartPosition;
  }

  public boolean isEmpty() {
    return stack.empty();
  }

  private void pushToStack(String path, int lineNumber, int startPosition) {
    if (!inheritFromParent && stack.empty()) {
      topLineNumber = lineNumber;
      topStartPosition = startPosition;
    }
    stack.push(path);
  }

  public boolean isInheritFromParent() {
    return inheritFromParent;
  }
}
