package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class TruncateHtmlFilterTest {

  TruncateHtmlFilter filter;
  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    filter = new TruncateHtmlFilter();
    interpreter = mock(JinjavaInterpreter.class);
  }

  @Test
  public void itPreservesEndTagsWhenTruncatingWithinTagContent() {
    String result = (String) filter.filter(fixture("filter/truncatehtml/long-content-with-tags.html"), interpreter, "33");
    assertThat(result).isEqualTo("<h1>HTML Ipsum Presents</h1> \n<p><strong>Pellentesque...</strong></p>");
  }

  @Test
  public void itDoesntChopWordsWhenSpecified() {
    String result = (String) filter.filter(fixture("filter/truncatehtml/long-content-with-tags.html"), interpreter, "35");
    assertThat(result).isEqualTo("<h1>HTML Ipsum Presents</h1> \n<p><strong>Pellentesque...</strong></p>");

    result = (String) filter.filter(fixture("filter/truncatehtml/long-content-with-tags.html"), interpreter, "35", "...", "true");
    assertThat(result).isEqualTo("<h1>HTML Ipsum Presents</h1> \n<p><strong>Pellentesque ha...</strong></p>");
  }

  private static String fixture(String name) {
    try {
      return Resources.toString(Resources.getResource(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
