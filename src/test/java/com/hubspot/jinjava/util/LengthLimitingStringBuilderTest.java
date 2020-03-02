package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.interpret.OutputTooBigException;
import org.junit.Test;

public class LengthLimitingStringBuilderTest {

  @Test
  public void itLimitsStringLength() throws Exception {
    LengthLimitingStringBuilder sb = new LengthLimitingStringBuilder(10);
    sb.append("0123456789");
    assertThatThrownBy(() -> sb.append("1")).isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itDoesNotLimitWithZeroLength() throws Exception {
    LengthLimitingStringBuilder sb = new LengthLimitingStringBuilder(0);
    sb.append("0123456789");
  }
}
