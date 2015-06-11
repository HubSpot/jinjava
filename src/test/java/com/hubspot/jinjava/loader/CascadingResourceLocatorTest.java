package com.hubspot.jinjava.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CascadingResourceLocatorTest {

  @Mock
  ResourceLocator first;
  @Mock
  ResourceLocator second;
  @Mock
  JinjavaInterpreter interpreter;

  CascadingResourceLocator locator;

  @Before
  public void setup() {
    locator = new CascadingResourceLocator(first, second);
  }

  @Test
  public void itUsesResponseFromFirstIfPresent() throws Exception {
    when(first.getString("foo", StandardCharsets.UTF_8, interpreter)).thenReturn("bar");
    assertThat(locator.getString("foo", StandardCharsets.UTF_8, interpreter)).isEqualTo("bar");
  }

  @Test
  public void itUsesResponseFromSecondWhenNotFoundOnFirst() throws Exception {
    when(first.getString("foo", StandardCharsets.UTF_8, interpreter)).thenThrow(ResourceNotFoundException.class);
    when(second.getString("foo", StandardCharsets.UTF_8, interpreter)).thenReturn("bar");
    assertThat(locator.getString("foo", StandardCharsets.UTF_8, interpreter)).isEqualTo("bar");
  }

  @Test(expected = ResourceNotFoundException.class)
  public void notFoundWhenAllLocatorsReturnNotFound() throws Exception {
    when(first.getString("foo", StandardCharsets.UTF_8, interpreter)).thenThrow(ResourceNotFoundException.class);
    when(second.getString("foo", StandardCharsets.UTF_8, interpreter)).thenThrow(ResourceNotFoundException.class);
    locator.getString("foo", StandardCharsets.UTF_8, interpreter);
  }
}
