package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SplitFilterTest {

  @Mock
  JinjavaInterpreter interpreter;
  SplitFilter filter;

  @Before
  public void setup() {
    filter = new SplitFilter();
  }

  @Test
  public void itDefaultsToSpaceSep() {
    List<String> result = (List<String>) filter.filter("hello world  this is fred", interpreter);
    assertThat(result).containsExactly("hello", "world", "this", "is", "fred");
  }

  @Test
  public void itUsesDifferentSeparatorIfSpecified() {
    List<String> result = (List<String>) filter.filter("hello world,  this is fred", interpreter, ",");
    assertThat(result).containsExactly("hello world", "this is fred");
  }

  @Test
  public void itLimitsResultIfSpecified() {
    List<String> result = (List<String>) filter.filter("hello world  this is fred", interpreter, " ", "2");
    assertThat(result).containsExactly("hello", "world  this is fred");
  }

}
