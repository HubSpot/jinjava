package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class RangeFunctionTest {

  @Test
  public void itGeneratesSimpleRanges() {
    assertThat(Functions.range(1)).isEqualTo(Collections.singletonList(0));
    assertThat(Functions.range(2)).isEqualTo(Arrays.asList(0, 1));
    assertThat(Functions.range(2, 4)).isEqualTo(Arrays.asList(2, 3));
    assertThat(Functions.range("2", "4")).isEqualTo(Arrays.asList(2, 3));
    assertThat(Functions.range(2, 8, 2)).isEqualTo(Arrays.asList(2, 4, 6));
  }

  @Test
  public void itGeneratesBackwardsRanges() {
    assertThat(Functions.range(2, -1, -1)).isEqualTo(Arrays.asList(2, 1, 0));
    assertThat(Functions.range(8, 2, -2)).isEqualTo(Arrays.asList(8, 6, 4));
    assertThat(Functions.range(2, -1, "-1")).isEqualTo(Arrays.asList(2, 1, 0));
  }

  @Test
  public void itHandlesBadRanges() {
    assertThat(Functions.range(-2)).isEmpty();
    assertThat(Functions.range(-2, -4)).isEmpty();
    assertThat(Functions.range(-2, -2)).isEmpty();
    assertThat(Functions.range(2, 2)).isEmpty();
    assertThat(Functions.range(2, 2000, 0)).isEmpty();
    assertThat(Functions.range(2, 2000, -5)).isEmpty();
  }

  @Test
  public void itHandlesBadValues() {
    assertThat(Functions.range(2, "f")).isEmpty();
  }

  @Test
  public void itTruncatesHugeRanges() {
    assertThat(Functions.range(2, 200000000).size()).isEqualTo(Functions.RANGE_LIMIT);
  }

}
