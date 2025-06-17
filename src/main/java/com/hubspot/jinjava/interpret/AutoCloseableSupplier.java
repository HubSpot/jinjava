package com.hubspot.jinjava.interpret;

import com.google.common.base.Suppliers;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AutoCloseableSupplier<T> implements Supplier<AutoCloseableImpl<T>> {

  public static <T> AutoCloseableSupplier<T> of(T tSupplier) {
    return of(() -> tSupplier, ignored -> {});
  }

  public static <T> AutoCloseableSupplier<T> of(
    Supplier<T> tSupplier,
    Consumer<T> closeConsumer
  ) {
    return new AutoCloseableSupplier<>(
      Suppliers.memoize(() -> new AutoCloseableImpl<>(tSupplier.get(), closeConsumer))
    );
  }

  private final Supplier<AutoCloseableImpl<T>> autoCloseableImplWrapper;

  private AutoCloseableSupplier(Supplier<AutoCloseableImpl<T>> autoCloseableImplWrapper) {
    this.autoCloseableImplWrapper = autoCloseableImplWrapper;
  }

  @Override
  public AutoCloseableImpl<T> get() {
    return autoCloseableImplWrapper.get();
  }

  public T dangerouslyGetWithoutClosing() {
    return autoCloseableImplWrapper.get().value();
  }

  public <R> AutoCloseableSupplier<R> map(Function<T, R> mapper) {
    return new AutoCloseableSupplier<>(() -> {
      T t = autoCloseableImplWrapper.get().value();
      return new AutoCloseableImpl<>(
        mapper.apply(t),
        r -> autoCloseableImplWrapper.get().closeConsumer.accept(t)
      );
    });
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
