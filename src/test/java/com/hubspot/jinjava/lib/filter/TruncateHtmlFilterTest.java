package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.features.BuiltInFeatures;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;

public class TruncateHtmlFilterTest extends BaseInterpretingTest {

  TruncateHtmlFilter filter;
  JinjavaInterpreter preserveVoidInterpreter;

  @Before
  public void setup() {
    filter = new TruncateHtmlFilter();
    preserveVoidInterpreter =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withFeatureConfig(
            FeatureConfig
              .newBuilder()
              .add(
                BuiltInFeatures.PRESERVE_VOID_IN_TRUNCATE_HTML,
                FeatureStrategies.ACTIVE
              )
              .build()
          )
          .build()
      )
        .newInterpreter();
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
      preserveVoidInterpreter,
      "255"
    );
    assertThat(result)
      .isEqualTo("<p>Hey,\n <br>\n Great to hear!</p>\n<p><img src=\"a.png\"></p>");
  }

  @Test
  public void itPreservesVoidElementsBeforeTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hello<br>world, this is long text</p>",
      preserveVoidInterpreter,
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
      preserveVoidInterpreter,
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
      preserveVoidInterpreter,
      "255"
    );
    assertThat(result).isEqualTo("<p>Hey,\n <br>\n Great to hear!</p>");
  }

  @Test
  public void itRemovesSelfClosingBrTagsAfterTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hello world<br/>more text</p><br/>",
      preserveVoidInterpreter,
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
      preserveVoidInterpreter,
      "255"
    );
    assertThat(result).isEqualTo("<p><img src=\"a.png\" width=\"10\">Great to hear!</p>");
  }

  @Test
  public void itRemovesVoidElementsWithExplicitClosingTagsAfterTheTruncationPoint() {
    String result = (String) filter.filter(
      "<p>Hello world</p><p><img src=\"a.png\" width=\"10\"></img></p>",
      preserveVoidInterpreter,
      "5",
      "...",
      "true"
    );
    assertThat(result).isEqualTo("<p>Hello...</p>");
  }

  @Test
  public void itRemovesVoidElementsWhenFeatureIsInactive() {
    String result = (String) filter.filter(
      "<p>Hey,<br>Great to hear!</p><p><img src=\"a.png\"></p>",
      interpreter,
      "255"
    );
    assertThat(result).isEqualTo("<p>Hey,Great to hear!</p>");
  }

  @Test
  public void itRemovesVoidElementsBeforeTheTruncationPointWhenFeatureIsInactive() {
    String result = (String) filter.filter(
      "<p>Hello<br>world, this is long text</p>",
      interpreter,
      "10",
      "...",
      "true"
    );
    assertThat(result).isEqualTo("<p>Helloworld...</p>");
  }

  private static String fixture(String name) {
    try {
      return Resources.toString(Resources.getResource(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
