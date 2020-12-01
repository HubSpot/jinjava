package com.hubspot.jinjava.util;

import com.hubspot.jinjava.interpret.OutputTooBigException;
import java.util.StringJoiner;

public class LengthLimitingStringJoiner {
  private final StringJoiner joiner;
  private final int delimiterLength;
  private final long maxLength;

  public LengthLimitingStringJoiner(long maxLength, CharSequence delimiter) {
    joiner = new StringJoiner(delimiter);
    delimiterLength = delimiter.length();
    this.maxLength = maxLength;
  }

  public int length() {
    return joiner.length();
  }

  public LengthLimitingStringJoiner add(Object obj) {
    return add(String.valueOf(obj));
  }

  public LengthLimitingStringJoiner add(CharSequence newElement) {
    long newLength =
      joiner.length() + newElement.length() + (joiner.length() > 0 ? delimiterLength : 0);
    if (maxLength > 0 && newLength > maxLength) {
      throw new OutputTooBigException(maxLength, newLength);
    }
    joiner.add(newElement);
    return this;
  }
}
