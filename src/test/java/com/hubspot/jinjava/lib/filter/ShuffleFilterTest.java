package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.random.ConstantZeroRandomNumberGenerator;

public class ShuffleFilterTest {

  ShuffleFilter filter = new ShuffleFilter();

  JinjavaInterpreter interpreter = mock(JinjavaInterpreter.class);

  @SuppressWarnings("unchecked")
  @Test
  public void itShufflesItems() {

    when(interpreter.getRandom()).thenReturn(ThreadLocalRandom.current());

    List<String> before = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    List<String> after = (List<String>) filter.filter(before, interpreter);

    assertThat(before).isSorted();
    assertThat(after).containsAll(before);

    try {
      assertThat(after).isSorted();
      failBecauseExceptionWasNotThrown(AssertionError.class);
    } catch (AssertionError e) {
      assertThat(e).hasMessageContaining("is not sorted");
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void itShufflesConsistentlyWithConstantRandom() {

    when(interpreter.getRandom()).thenReturn(new ConstantZeroRandomNumberGenerator());

    List<String> before = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    List<String> after = (List<String>) filter.filter(before, interpreter);

    assertThat(before).isSorted();
    assertThat(after).containsAll(before);

    assertThat(after).containsExactly("2", "3", "4", "5", "6", "7", "8", "9", "1");
  }

}
