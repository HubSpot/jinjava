package com.hubspot.jinjava.util;

import com.hubspot.jinjava.interpret.OutputTooBigException;
import java.io.Serializable;
import java.util.stream.IntStream;

public class LengthLimitingStringBuilder
  implements Serializable, CharSequence, Appendable {

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

  @Override
  public LengthLimitingStringBuilder append(CharSequence csq) {
    int csqLength = 4; // null
    if (csq != null) {
      csqLength = csq.length();
    }
    length += csqLength;
    checkLength();
    builder.append(csq);
    return this;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) {
    int csqLength = 4; // null
    if (csq != null) {
      csqLength = end - start;
    }
    length += csqLength;
    checkLength();
    builder.append(csq, start, end);
    return this;
  }

  @Override
  public Appendable append(char c) {
    length++;
    checkLength();
    builder.append(c);
    return this;
  }

  private void checkLength() {
    if (maxLength > 0 && length > maxLength) {
      throw new OutputTooBigException(maxLength, length);
    }
  }
}
