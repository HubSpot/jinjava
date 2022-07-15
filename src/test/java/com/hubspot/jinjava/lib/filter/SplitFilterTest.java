package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class SplitFilterTest extends BaseInterpretingTest {
  SplitFilter filter;

  @Before
  public void setup() {
    filter = new SplitFilter();
  }

  @Test
  public void itDefaultsToSpaceSep() {
    List<String> result = (List<String>) filter.filter(
      "hello world  this is fred",
      interpreter
    );
    assertThat(result).containsExactly("hello", "world", "this", "is", "fred");
  }

  @Test
  public void itUsesDifferentSeparatorIfSpecified() {
    List<String> result = (List<String>) filter.filter(
      "hello world,  this is fred",
      interpreter,
      ","
    );
    assertThat(result).containsExactly("hello world", "this is fred");
  }

  @Test
  public void itLimitsResultIfSpecified() {
    List<String> result = (List<String>) filter.filter(
      "hello world  this is fred",
      interpreter,
      " ",
      "2"
    );
    assertThat(result).containsExactly("hello", "world  this is fred");
  }

  @Test
  public void itReturnsDefaultIfSeparatorIsNull() {
    List<String> result = (List<String>) filter.filter(
      "hello world  this is fred",
      interpreter,
      null
    );
    assertThat(result).containsExactly("hello", "world", "this", "is", "fred");
  }

  @Test
  public void itReturnsDefaultSeparatorIfNullAndTruncated() {
    List<String> result = (List<String>) filter.filter(
      "hello world  this is fred",
      interpreter,
      null,
      "2"
    );
    assertThat(result).containsExactly("hello", "world  this is fred");
  }
}
