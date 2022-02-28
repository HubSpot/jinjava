package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.interpret.OutputTooBigException;
import org.junit.Test;

public class LengthLimitingStringBuilderTest {

  @Test
  public void itLimitsStringLength() {
    LengthLimitingStringBuilder sb = new LengthLimitingStringBuilder(10);
    sb.append("0123456789");
    assertThatThrownBy(() -> sb.append("1")).isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itDoesNotLimitWithZeroLength() {
    LengthLimitingStringBuilder sb = new LengthLimitingStringBuilder(0);
    sb.append("0123456789");
  }

  @Test
  public void itHandlesNullStrings() {
    LengthLimitingStringBuilder sb = new LengthLimitingStringBuilder(10);
    sb.append(null);
    assertThat(sb.length()).isEqualTo(0);
  }
}
