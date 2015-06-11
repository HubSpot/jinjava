package com.hubspot.jinjava.loader;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ClasspathResourceLocatorTest {

  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
  }

  @Test
  public void testLoadFromClasspath() throws Exception {
    assertThat(new ClasspathResourceLocator().getString("loader/cp/foo/bar.jinja", StandardCharsets.UTF_8, interpreter)).isEqualTo("hello world.");
  }

  @Test(expected = ResourceNotFoundException.class)
  public void itThrowsNotFoundWhenNotFound() throws Exception {
    new ClasspathResourceLocator().getString("foo", StandardCharsets.UTF_8, interpreter);
  }

}
