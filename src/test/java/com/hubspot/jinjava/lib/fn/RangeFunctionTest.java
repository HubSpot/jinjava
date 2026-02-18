package com.hubspot.jinjava.lib.fn;

import static com.hubspot.jinjava.interpret.JinjavaInterpreter.pushCurrent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RangeFunctionTest {

  private JinjavaConfig config;

  @Before
  public void beforeEach() {
    config =
      JinjavaConfig
        .newBuilder()
        .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
        .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
        .build();
    Jinjava jinjava = new Jinjava(config);
    pushCurrent(new JinjavaInterpreter(jinjava.newInterpreter()));
  }

  @After
  public void afterEach() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void interpreterInstanceIsMandatory() {
    JinjavaInterpreter.popCurrent();
    assertThatThrownBy(() -> Functions.range(1)).isInstanceOf(NullPointerException.class);
  }

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
    assertThat(Functions.range("f")).isEmpty();
    assertThat(Functions.range(2, "f")).isEmpty();
    assertThat(Functions.range(2, new Object[] { null })).isEmpty();
    assertThat(Functions.range(2, 4, null)).isEqualTo(Arrays.asList(2, 3));
  }

  @Test
  public void itHandlesMissingArg() {
    assertThatThrownBy(() -> Functions.range(null))
      .isInstanceOf(InvalidArgumentException.class);
  }

  @Test
  public void itTruncatesRangeToDefaultRangeLimit() {
    int defaultRangeLimit = config.getRangeLimit();
    assertThat(defaultRangeLimit).isEqualTo(Functions.DEFAULT_RANGE_LIMIT);
    assertThat(Functions.range(2, 200000000).size()).isEqualTo(defaultRangeLimit);
    assertThat(Functions.range(Long.MAX_VALUE - 1, Long.MAX_VALUE).size())
      .isEqualTo(defaultRangeLimit);
  }

  @Test
  public void itTruncatesRangeToCustomRangeLimit() {
    JinjavaInterpreter.popCurrent();
    int customRangeLimit = 10;
    JinjavaConfig customConfig = JinjavaConfig
      .newBuilder()
      .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
      .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
      .withRangeLimit(customRangeLimit)
      .build();
    pushCurrent(new JinjavaInterpreter(new Jinjava(customConfig).newInterpreter()));
    assertThat(customConfig.getRangeLimit()).isEqualTo(customRangeLimit);

    assertThat(Functions.range(20).size()).isEqualTo(customRangeLimit);
    assertThat(Functions.range(Long.MAX_VALUE - 1, Long.MAX_VALUE).size())
      .isEqualTo(customRangeLimit);
  }
}
