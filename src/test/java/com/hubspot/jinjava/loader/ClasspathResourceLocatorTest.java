package com.hubspot.jinjava.loader;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class ClasspathResourceLocatorTest extends BaseInterpretingTest {

  @Test
  public void testLoadFromClasspath() throws Exception {
    assertThat(
      new ClasspathResourceLocator()
        .getString("loader/cp/foo/bar.jinja", StandardCharsets.UTF_8, interpreter)
    )
      .isEqualTo("hello world.");
  }

  @Test(expected = ResourceNotFoundException.class)
  public void itThrowsNotFoundWhenNotFound() throws Exception {
    new ClasspathResourceLocator().getString("foo", StandardCharsets.UTF_8, interpreter);
  }
}
