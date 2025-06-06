package com.hubspot.jinjava.interpret;

import java.util.function.Consumer;

public class AutoCloseableWrapper<T> implements AutoCloseable {

  private final T t;
  private final Consumer<T> closeConsumer;

  public static <T> AutoCloseableWrapper<T> of(T t, Consumer<T> closeConsumer) {
    return new AutoCloseableWrapper<>(t, closeConsumer);
  }

  protected AutoCloseableWrapper(T t, Consumer<T> closeConsumer) {
    this.t = t;
    this.closeConsumer = closeConsumer;
  }

  public T get() {
    return t;
  }

  @Override
  public void close() {
    closeConsumer.accept(t);
  }
}
