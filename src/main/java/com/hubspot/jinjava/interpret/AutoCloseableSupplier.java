package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoCloseableSupplier<T> implements Supplier<AutoCloseableImpl<T>> {

  public interface GenericThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
  }

  public static <T> AutoCloseableSupplier<T> of(T t) {
    return new AutoCloseableSupplier<>(new AutoCloseableImpl<>(t, ignored -> {}));
  }

  public static <T> AutoCloseableSupplier<T> of(T t, Consumer<T> closeConsumer) {
    return new AutoCloseableSupplier<>(new AutoCloseableImpl<>(t, closeConsumer));
  }

  private final AutoCloseableImpl<T> autoCloseableImplWrapper;

  private AutoCloseableSupplier(AutoCloseableImpl<T> autoCloseableImplWrapper) {
    this.autoCloseableImplWrapper = autoCloseableImplWrapper;
  }

  @Override
  public AutoCloseableImpl<T> get() {
    return autoCloseableImplWrapper;
  }

  public T dangerouslyGetWithoutClosing() {
    return autoCloseableImplWrapper.value();
  }

  public <R, E extends Exception> AutoCloseableSupplier<R> map(
    GenericThrowingFunction<T, R, E> mapper
  ) throws E {
    T t = autoCloseableImplWrapper.value();
    return new AutoCloseableSupplier<>(
      new AutoCloseableImpl<>(
        mapper.apply(t),
        r -> autoCloseableImplWrapper.closeConsumer.accept(t)
      )
    );
  }

  public static class AutoCloseableImpl<T> implements java.lang.AutoCloseable {

    private final T t;
    private final Consumer<T> closeConsumer;

    protected AutoCloseableImpl(T t, Consumer<T> closeConsumer) {
      this.t = t;
      this.closeConsumer = closeConsumer;
    }

    public T value() {
      return t;
    }

    @Override
    public void close() {
      closeConsumer.accept(t);
    }
  }
}
