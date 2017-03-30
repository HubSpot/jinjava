package com.hubspot.jinjava.interpret;

public class OutputTooBigException extends RuntimeException {

  private final long size;

  public OutputTooBigException(long size) {
    this.size = size;
  }

  @Override
  public String getMessage() {
    return String.format("%d byte output rendered", size);
  }

}
