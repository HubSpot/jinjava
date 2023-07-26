package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StripTagsFilterTest {
  private JinjavaInterpreter interpreter;

  @InjectMocks
  private StripTagsFilter filter;

  @Before
  public void setup() {
    JinjavaConfig config = JinjavaConfig.newBuilder().build();
    Jinjava jinjava = new Jinjava(config);
    this.interpreter = new JinjavaInterpreter(jinjava.newInterpreter());
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itPassesThruNonStringVals() throws Exception {
    assertThat(filter.filter(123, interpreter)).isEqualTo(123);
    assertThat(filter.filter(true, interpreter)).isEqualTo(true);
    Object foo = new Object();
    assertThat(filter.filter(foo, interpreter)).isSameAs(foo);
  }

  @Test
  public void itWorksWithNonHtmlStrings() throws Exception {
    assertThat(filter.filter("foo", interpreter)).isEqualTo("foo");
    assertThat(filter.filter("foo < bar", interpreter)).isEqualTo("foo &lt; bar");
  }

  @Test
  public void itNormalizesWhitespaceInNonHtmlStrings() throws Exception {
    assertThat(filter.filter("foo bar  other   var", interpreter))
      .isEqualTo("foo bar other var");
  }

  @Test
  public void itStripsTagsFromHtml() throws Exception {
    assertThat(filter.filter("foo <b>bar</b> other", interpreter))
      .isEqualTo("foo bar other");
  }

  @Test
  public void itStripsTagsFromNestedHtml() throws Exception {
    assertThat(filter.filter("<div><strong>test</strong></div>", interpreter))
      .isEqualTo("test");
  }

  @Test
  public void itStripsTagsFromEscapedHtml() throws Exception {
    assertThat(filter.filter("&lt;div&gt;test&lt;/test&gt;", interpreter))
      .isEqualTo("test");
  }

  @Test
  public void itPreservesBreaks() throws Exception {
    assertThat(filter.filter("<p>Test!<br><br>Space</p>", interpreter))
      .isEqualTo("Test! Space");
  }

  @Test
  public void itConvertsNewlinesToSpaces() throws Exception {
    assertThat(filter.filter("<p>Test!\n\nSpace</p>", interpreter))
      .isEqualTo("Test! Space");
  }

  @Test
  public void itHandlesNonBreakSpaces() {
    assertThat(filter.filter("Test&nbsp;Value", interpreter)).isEqualTo("Test Value");
  }

  @Test
  public void itAddsWhitespaceBetweenParagraphTags() {
    assertThat(filter.filter("<p>Test</p><p>Value</p>", interpreter))
      .isEqualTo("Test Value");
  }

  @Test
  public void itExecutesJinjavaInsideTag() {
    assertThat(
        filter.filter("{% for i in [1, 2, 3] %}<div>{{i}}</div>{% endfor %}", interpreter)
      )
      .isEqualTo("1 2 3");
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
      .thenAnswer(
        i ->
          counter.getAndIncrement() == 0
            ? Collections.emptySet()
            : Collections.singleton(
              DeferredToken
                .builderFromToken(
                  new ExpressionToken(
                    "{{ deferred && other }}",
                    0,
                    0,
                    new DefaultTokenScannerSymbols()
                  )
                )
                .build()
            )
      );
    assertThatThrownBy(() -> filter.filter("{{ deferred && other }}", mockedInterpreter))
      .isInstanceOf(DeferredValueException.class);
  }
}
