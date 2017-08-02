package com.hubspot.jinjava.random;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * A random number generator that always returns 0. Useful for testing code when you want the output to be constant.
 */
public class ConstantZeroRandomNumberGenerator extends Random {

  @Override
  protected int next(int bits) {
    return 0;
  }

  @Override
  public int nextInt() {
    return 0;
  }

  @Override
  public int nextInt(int bound) {
    return 0;
  }

  @Override
  public long nextLong() {
    return 0;
  }

  @Override
  public boolean nextBoolean() {
    return false;
  }

  @Override
  public float nextFloat() {
    return 0f;
  }

  @Override
  public double nextDouble() {
    return 0;
  }

  @Override
  public synchronized double nextGaussian() {
    return 0;
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
  public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
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
  public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
    throw new UnsupportedOperationException();
  }
}
