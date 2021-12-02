package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.random.ConstantZeroRandomNumberGenerator;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Before;
import org.junit.Test;

public class RandomFilterTest {
  RandomFilter filter = new RandomFilter();

  JinjavaInterpreter interpreter = mock(JinjavaInterpreter.class);
  JinjavaConfig config = mock(JinjavaConfig.class);

  @Before
  public void setUp() throws Exception {
    when(interpreter.getRandom()).thenReturn(ThreadLocalRandom.current());
    when(interpreter.getConfig()).thenReturn(config);
    when(config.getRandomNumberGeneratorStrategy())
      .thenReturn(RandomNumberGeneratorStrategy.THREAD_LOCAL);
  }

  @Test
  public void itReturnsNullWithNullObject() {
    assertThat(filter.filter(null, interpreter)).isNull();
  }

  @Test
  public void itTakesRandomElementFromList() {
    Collection<Integer> ints = Arrays.asList(1, 2);
    Object random = filter.filter(ints, interpreter);
    assertThat((Integer) random).isBetween(1, 2);
  }

  @Test
  public void itReturnsNullFromEmptyList() {
    assertThat(filter.filter(Collections.emptyList(), interpreter)).isNull();
  }

  @Test
  public void itAlwaysUsesFirstListValueWhenUsingConstantRandom() {
    setUpConstantRandom();

    Collection<Integer> ints = Arrays.asList(1, 2);
    assertThat((Integer) filter.filter(ints, interpreter)).isEqualTo(1);
  }

  @Test
  public void itTakesRandomElementFromArray() {
    assertThat((Integer) filter.filter(new Integer[] { 1, 2 }, interpreter))
      .isBetween(1, 2);
  }

  @Test
  public void itReturnsNullFromEmptyArray() {
    assertThat(filter.filter(new Integer[] {}, interpreter)).isNull();
  }

  @Test
  public void itAlwaysUsesFirstArrayValueWhenUsingConstantRandom() {
    setUpConstantRandom();
    assertThat((Integer) filter.filter(new Integer[] { 1, 2 }, interpreter)).isEqualTo(1);
  }

  private void setUpConstantRandom() {
    when(interpreter.getRandom()).thenReturn(new ConstantZeroRandomNumberGenerator());
    when(interpreter.getConfig()).thenReturn(config);
    when(config.getRandomNumberGeneratorStrategy())
      .thenReturn(RandomNumberGeneratorStrategy.CONSTANT_ZERO);
  }

  @Test
  public void itTakesRandomElementFromMap() {
    LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
    map.put(1, 3);
    map.put(2, 4);
    assertThat((Integer) filter.filter(map, interpreter)).isBetween(3, 4);
  }

  @Test
  public void itReturnsNullFromEmptyMap() {
    assertThat(filter.filter(Collections.emptyMap(), interpreter)).isNull();
  }

  @Test
  public void itAlwaysUsesFirstMapValueWhenUsingConstantRandom() {
    setUpConstantRandom();
    LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
    map.put(1, 3);
    map.put(2, 4);
    assertThat((Integer) filter.filter(map, interpreter)).isEqualTo(3);
  }

  @Test
  public void itGeneratesRandomNumberInRange() {
    assertThat((Integer) filter.filter(2, interpreter)).isBetween(0, 2);
  }

  @Test
  public void itAlwaysReturnsRangeWhenUsingConstantRandom() {
    setUpConstantRandom();
    assertThat((Integer) filter.filter(3, interpreter)).isEqualTo(0);
  }

  @Test
  public void itGeneratesRandomNumberWithStringRange() {
    assertThat((Integer) filter.filter("2.0", interpreter)).isBetween(0, 2);
  }

  @Test
  public void itReturnsZeroWithBadStringRange() {
    assertThat((Integer) filter.filter("what", interpreter)).isEqualTo(0);
  }

  @Test
  public void itAlwaysReturnsZeroWhenUsingConstantRandom() {
    setUpConstantRandom();
    assertThat((Integer) filter.filter("whut", interpreter)).isEqualTo(0);
  }

  @Test
  public void itReturnsOriginalObjectForOtherClasses() {
    assertThat((JinjavaInterpreter) filter.filter(interpreter, interpreter))
      .isEqualTo(interpreter);
  }
}
