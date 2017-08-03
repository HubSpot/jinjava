package com.hubspot.jinjava.util;

import java.io.Serializable;
import java.util.stream.IntStream;

import com.hubspot.jinjava.interpret.OutputTooBigException;

public class LengthLimitingStringBuilder implements Serializable, CharSequence {

  private static final long serialVersionUID = -1891922886257965755L;

  private final StringBuilder builder;
  private long length = 0;
  private final long maxLength;

  public LengthLimitingStringBuilder(long maxLength) {
    builder = new StringBuilder();
    this.maxLength = maxLength;
  }

  @Override
  public int length() {
    return builder.length();
  }

  @Override
  public char charAt(int index) {
    return builder.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return builder.subSequence(start, end);
  }

  @Override
  public String toString() {
    return builder.toString();
  }

  @Override
  public IntStream chars() {
    return builder.chars();
  }

  @Override
  public IntStream codePoints() {
    return builder.codePoints();
  }

  public void append(Object obj) {
    append(String.valueOf(obj));
  }

  public void append(String str) {
    length += str.length();
    if (maxLength > 0 && length > maxLength) {
      throw new OutputTooBigException(maxLength, length);
    }
    builder.append(str);
  }
}
