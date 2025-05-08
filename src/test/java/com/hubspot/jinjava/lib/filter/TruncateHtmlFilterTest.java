package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
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
  public void itExecutesJinjavaInsideTag() {
    assertThat(
      filter.filter("{% for i in [1, 2, 3] %}<div>{{i}}</div>{% endfor %}", interpreter)
    )
      .isEqualTo("<div>\n 1\n</div>\n<div>\n 2\n</div>\n<div>\n 3\n</div>");
  }

  @Test
  public void itExecutesJinjavaInsideTagAndTruncates() {
    assertThat(
      filter.filter(
        "{% for i in [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12] %}<div>{{i}}</div>{% endfor %}",
        interpreter,
        "10"
      )
    )
      .isEqualTo(
        "<div>\n 1\n</div>\n<div>\n 2\n</div>\n<div>\n 3\n</div>\n<div>\n 4\n</div>\n<div>\n 5\n</div>\n" +
        "<div>\n 6\n</div>\n<div>\n 7\n</div>\n<div>\n 8\n</div>\n<div>\n 9\n</div>\n<div>\n ...\n</div>"
      );
  }

  @Test
  public void itIsolatesJinjavaScopeWhenExecutingCodeInsideTag() {
    filter.filter("{% set test = 'hello' %}", interpreter);
    assertThat(interpreter.getContext().get("test")).isNull();
  }

  @Test
  public void itThrowsDeferredValueExceptionWhenDeferredTokensAreLeft() {
    AtomicInteger counter = new AtomicInteger();
    JinjavaInterpreter mockedInterpreter = mock(JinjavaInterpreter.class);
    Context mockedContext = mock(Context.class);
    when(mockedInterpreter.getContext()).thenReturn(mockedContext);
    when(mockedContext.getDeferredTokens())
      .thenAnswer(i ->
        counter.getAndIncrement() == 0
          ? Collections.emptySet()
          : Collections.singleton(
            DeferredToken
              .builderFromImage(
                "{{ deferred && other }}",
                ExpressionToken.class,
                interpreter
              )
              .build()
          )
      );
    assertThatThrownBy(() -> filter.filter("{{ deferred && other }}", mockedInterpreter))
      .isInstanceOf(DeferredValueException.class);
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

  private static String fixture(String name) {
    try {
      return Resources.toString(Resources.getResource(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
