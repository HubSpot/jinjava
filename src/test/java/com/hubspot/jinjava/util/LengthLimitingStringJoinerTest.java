package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.interpret.OutputTooBigException;
import org.junit.Test;

public class LengthLimitingStringJoinerTest {

  @Test
  public void itLimitsStringLength() {
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(10, " ");
    joiner.add("0123456789");
    assertThatThrownBy(() -> joiner.add("1")).isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itDoesNotLimitWithZeroLength() {
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(0, " ");
    joiner.add("0123456789");
  }

  @Test
  public void itLimitsOnAdd() {
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(1, " ");
    assertThatThrownBy(() -> joiner.add("123")).isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itIncludesDelimiterInLimit() {
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(5, "123456789");
    joiner.add('a');
    assertThatThrownBy(() -> joiner.add('b')).isInstanceOf(OutputTooBigException.class);
  }
}
