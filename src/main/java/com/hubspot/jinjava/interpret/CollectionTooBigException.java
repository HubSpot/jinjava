package com.hubspot.jinjava.interpret;

public class CollectionTooBigException extends RuntimeException {

  private final int maxSize;
  private final int size;

  public CollectionTooBigException(int size, int maxSize) {
    this.maxSize = maxSize;
    this.size = size;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public int getSize() {
    return size;
  }

  @Override
  public String getMessage() {
    return String.format(
      "Collection of size %d is greater than the maximum size of %d",
      size,
      maxSize
    );
  }
}
