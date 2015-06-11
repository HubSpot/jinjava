package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ShuffleFilterTest {

  ShuffleFilter filter;

  @Before
  public void setup() {
    this.filter = new ShuffleFilter();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shuffleItems() {
    List<String> before = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    List<String> after = (List<String>) filter.filter(before, null);

    assertThat(before).isSorted();
    assertThat(after).containsAll(before);

    try {
      assertThat(after).isSorted();
      failBecauseExceptionWasNotThrown(AssertionError.class);
    } catch (AssertionError e) {
      assertThat(e).hasMessageContaining("is not sorted");
    }
  }

}
