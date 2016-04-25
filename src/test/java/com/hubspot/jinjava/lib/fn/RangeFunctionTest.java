package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class RangeFunctionTest {

  @Test
  public void itGeneratesSimpleRanges() {
    assertThat(Functions.range(1)).isEqualTo(Arrays.asList(0));
    assertThat(Functions.range(2)).isEqualTo(Arrays.asList(0, 1));
    assertThat(Functions.range(2, 4)).isEqualTo(Arrays.asList(2, 3));
    assertThat(Functions.range(2, 8, 2)).isEqualTo(Arrays.asList(2, 4, 6));
  }

  @Test
  public void itGeneratesBackwardsRanges() {
    assertThat(Functions.range(-2)).isEqualTo(Arrays.asList(0, -1));
    assertThat(Functions.range(2, -1)).isEqualTo(Arrays.asList(2, 1, 0));
    assertThat(Functions.range(8, 2, -2)).isEqualTo(Arrays.asList(8, 6, 4));
  }

  @Test
  public void itHandlesBadRanges() {
    assertThat(Functions.range(2, 2)).isEqualTo(Arrays.asList());
    assertThat(Functions.range(2, 2000, -5).size()).isEqualTo(Functions.RANGE_LIMIT);
  }

  @Test
  public void itTruncatesHugeRanges() {
    assertThat(Functions.range(2, 200000000).size()).isEqualTo(Functions.RANGE_LIMIT);
  }

}
