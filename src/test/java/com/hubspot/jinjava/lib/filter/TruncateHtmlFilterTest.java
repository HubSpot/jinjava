package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;

public class TruncateHtmlFilterTest extends BaseInterpretingTest {
  TruncateHtmlFilter filter;

  @Before
  public void setup() {
    filter = new TruncateHtmlFilter();
  }

  @Test
  public void itPreservesEndTagsWhenTruncatingWithinTagContent() {
    String result = (String) filter.filter(
      fixture("filter/truncatehtml/long-content-with-tags.html"),
      interpreter,
      "33"
    );
    assertThat(result)
      .isEqualTo(
        "<h1>HTML Ipsum Presents</h1> \n<p><strong>Pellentesque...</strong></p>"
      );
  }

  @Test
  public void itDoesntChopWordsWhenSpecified() {
    String result = (String) filter.filter(
      fixture("filter/truncatehtml/long-content-with-tags.html"),
      interpreter,
      "35"
    );
    assertThat(result)
      .isEqualTo(
        "<h1>HTML Ipsum Presents</h1> \n<p><strong>Pellentesque...</strong></p>"
      );

    result =
      (String) filter.filter(
        fixture("filter/truncatehtml/long-content-with-tags.html"),
        interpreter,
        "35",
        "...",
        "true"
      );
    assertThat(result)
      .isEqualTo(
        "<h1>HTML Ipsum Presents</h1> \n<p><strong>Pellentesque ha...</strong></p>"
      );
  }

  @Test
  public void itTakesKwargs() {
    String result = (String) filter.filter(
      fixture("filter/truncatehtml/long-content-with-tags.html"),
      interpreter,
      new Object[] { "35" },
      ImmutableMap.of(TruncateHtmlFilter.BREAKWORD_KEY, false)
    );
    assertThat(result)
      .isEqualTo(
        "<h1>HTML Ipsum Presents</h1> \n<p><strong>Pellentesque...</strong></p>"
      );

    result =
      (String) filter.filter(
        fixture("filter/truncatehtml/long-content-with-tags.html"),
        interpreter,
        new Object[] { "35" },
        ImmutableMap.of(TruncateHtmlFilter.END_KEY, "TEST")
      );
    assertThat(result)
      .isEqualTo(
        "<h1>HTML Ipsum Presents</h1> \n<p><strong>PellentesqueTEST</strong></p>"
      );
  }

  @Test
  public void itDefaultsLengthWhenCannotBeParsed() {
    String result = (String) filter.filter(
      fixture("filter/truncatehtml/long-content-with-tags.html"),
      interpreter,
      new Object[] { "?" },
      ImmutableMap.of(TruncateHtmlFilter.BREAKWORD_KEY, false)
    );
    assertThat(result)
      .isEqualTo(
        "<h1>HTML Ipsum Presents</h1> \n" +
        "<p><strong>Pellentesque habitant morbi tristique</strong> senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit amet quam egestas semper. <em>Aenean ultricies...</em></p>"
      );
  }

  private static String fixture(String name) {
    try {
      return Resources.toString(Resources.getResource(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
