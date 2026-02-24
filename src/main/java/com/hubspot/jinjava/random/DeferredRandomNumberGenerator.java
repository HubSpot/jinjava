package com.hubspot.jinjava.random;

import com.hubspot.jinjava.interpret.DeferredValueException;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * A random number generator that throws {@link com.hubspot.jinjava.interpret.DeferredValueException} for all supported methods.
 */
public class DeferredRandomNumberGenerator extends Random {

  private static final String EXCEPTION_MESSAGE = "Generating random number";

  @Override
  protected int next(int bits) {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public int nextInt() {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public int nextInt(int bound) {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public long nextLong() {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public boolean nextBoolean() {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public float nextFloat() {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public double nextDouble() {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public synchronized double nextGaussian() {
    throw new DeferredValueException(EXCEPTION_MESSAGE);
  }

  @Override
  public void nextBytes(byte[] bytes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntStream ints(long streamSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntStream ints() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongStream longs(long streamSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongStream longs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongStream longs(
    long streamSize,
    long randomNumberOrigin,
    long randomNumberBound
  ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DoubleStream doubles(long streamSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DoubleStream doubles() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DoubleStream doubles(
    long streamSize,
    double randomNumberOrigin,
    double randomNumberBound
  ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
    throw new UnsupportedOperationException();
  }
}
