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
      .isEqualTo("<h1>HTML Ipsum Presents</h1>\n<p><strong>Pellentesque...</strong></p>");
  }

  @Test
  public void itDoesntChopWordsWhenSpecified() {
    String result = (String) filter.filter(
      fixture("filter/truncatehtml/long-content-with-tags.html"),
      interpreter,
      "35"
    );
    assertThat(result)
      .isEqualTo("<h1>HTML Ipsum Presents</h1>\n<p><strong>Pellentesque...</strong></p>");

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
        "<h1>HTML Ipsum Presents</h1>\n<p><strong>Pellentesque ha...</strong></p>"
      );
  }

  @Test
  public void itTakesKwargs() {
    String result = (String) filter.filter(
      fixture("filter/truncatehtml/long-content-with-tags.html"),
      interpreter,
      new Object[] { "35" },
      ImmutableMap.of("breakwords", false)
    );
    assertThat(result)
      .isEqualTo("<h1>HTML Ipsum Presents</h1>\n<p><strong>Pellentesque...</strong></p>");

    result =
      (String) filter.filter(
        fixture("filter/truncatehtml/long-content-with-tags.html"),
        interpreter,
        new Object[] { "35" },
        ImmutableMap.of("end", "TEST")
      );
    assertThat(result)
      .isEqualTo(
        "<h1>HTML Ipsum Presents</h1>\n<p><strong>PellentesqueTEST</strong></p>"
      );
  }

  @Test
  public void itPreservesVoidElementsWhenContentIsUnderTheLimit() {
    String result = (String) filter.filter(
      "<p>Hey,<br>Great to hear!</p><p><img src=\"a.png\"></p>",
      interpreter,
      "255"
    );
    assertThat(result)
      .isEqualTo("<p>Hey,\n <br>\n Great to hear!</p>\n<p><img src=\"a.png\"></p>");
  }

  @Test
  public void itPreservesVoidElementsBeforeTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hello<br>world, this is long text</p>",
      interpreter,
      "10",
      "...",
      "true"
    );
    assertThat(result).isEqualTo("<p>Hello\n <br>\n world...</p>");
  }

  @Test
  public void itRemovesVoidElementsAfterTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hello world</p><p>more<br>text</p><img src=\"a.png\">",
      interpreter,
      "5",
      "...",
      "true"
    );
    assertThat(result).isEqualTo("<p>Hello...</p>");
  }

  @Test
  public void itPreservesSelfClosingBrTagsBeforeTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hey,<br/>Great to hear!</p>",
      interpreter,
      "255"
    );
    assertThat(result).isEqualTo("<p>Hey,\n <br>\n Great to hear!</p>");
  }

  @Test
  public void itRemovesSelfClosingBrTagsAfterTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hello world<br/>more text</p><br/>",
      interpreter,
      "5",
      "...",
      "true"
    );
    assertThat(result).isEqualTo("<p>Hello...</p>");
  }

  @Test
  public void itPreservesVoidElementsWithExplicitClosingTagsBeforeTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p><img src=\"a.png\" width=\"10\"></img>Great to hear!</p>",
      interpreter,
      "255"
    );
    assertThat(result).isEqualTo("<p><img src=\"a.png\" width=\"10\">Great to hear!</p>");
  }

  @Test
  public void itRemovesVoidElementsWithExplicitClosingTagsAfterTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hello world</p><p><img src=\"a.png\" width=\"10\"></img></p>",
      interpreter,
      "5",
      "...",
      "true"
    );
    assertThat(result).isEqualTo("<p>Hello...</p>");
  }

  private static String fixture(String name) {
    try {
      return Resources.toString(Resources.getResource(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
