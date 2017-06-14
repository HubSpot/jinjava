package com.hubspot.jinjava.interpret;

public class OutputTooBigException extends RuntimeException {

  private long maxSize;
  private final long size;

  public OutputTooBigException(long maxSize, long size) {
    this.maxSize = maxSize;
    this.size = size;
  }

  @Override
  public String getMessage() {
    return String.format("%d byte output rendered, over limit of %d bytes", size, maxSize);
  }

}
