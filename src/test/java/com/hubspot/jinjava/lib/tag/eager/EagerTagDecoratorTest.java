package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.PreserveRawExecutionMode;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for the static methods in the EagerTagDecorator.
 */
@RunWith(MockitoJUnitRunner.class)
public class EagerTagDecoratorTest extends BaseInterpretingTest {

  @Test
  public void itExecutesInChildContextAndTakesNewValue() {
    context.put("foo", new ArrayList<Integer>());
    EagerStringResult result = EagerTagDecorator.executeInChildContext(
      (
        interpreter1 -> {
          ((List<Integer>) interpreter1.getContext().get("foo")).add(1);
          return "function return";
        }
      ),
      interpreter,
      true
    );
    assertThat(result.getResult()).isEqualTo("function return");
    assertThat(result.getPrefixToPreserveState()).isEqualTo("{% set foo = [1] %}");
    assertThat(context.get("foo")).isEqualTo(ImmutableList.of(1));
    assertThat(context.getEagerTokens()).isEmpty();
  }

  @Test
  public void itExecutesInChildContextAndDefersNewValue() {
    context.put("foo", new ArrayList<Integer>());
    EagerStringResult result = EagerTagDecorator.executeInChildContext(
      (
        interpreter1 -> {
          ((List<Integer>) interpreter1.getContext().get("foo")).add(1);
          return "function return";
        }
      ),
      interpreter,
      false
    );
    assertThat(result.getResult()).isEqualTo("function return");
    assertThat(result.getPrefixToPreserveState()).isEqualTo("{% set foo = [] %}");
    assertThat(context.get("foo")).isInstanceOf(DeferredValue.class);
    assertThat(context.getEagerTokens()).isNotEmpty();
  }

  @Test
  public void itBuildsSetTagForDeferredAndRegisters() {
    Map<String, String> deferredValuesToSet = ImmutableMap.of("foo", "'bar'");
    String result = EagerTagDecorator.buildSetTagForDeferredInChildContext(
      deferredValuesToSet,
      interpreter,
      true
    );
    assertThat(result).isEqualTo("{% set foo = 'bar' %}");
    assertThat(context.getEagerTokens()).hasSize(1);
    EagerToken eagerToken = context
      .getEagerTokens()
      .stream()
      .findAny()
      .orElseThrow(RuntimeException::new);
    assertThat(eagerToken.getSetDeferredWords()).containsExactly("foo");
    assertThat(eagerToken.getUsedDeferredWords()).isEmpty();
  }

  @Test
  public void itBuildsSetTagForDeferredAndDoesntRegister() {
    Map<String, String> deferredValuesToSet = ImmutableMap.of("foo", "'bar'");
    String result = EagerTagDecorator.buildSetTagForDeferredInChildContext(
      deferredValuesToSet,
      interpreter,
      false
    );
    assertThat(result).isEqualTo("{% set foo = 'bar' %}");
    assertThat(context.getEagerTokens()).isEmpty();
  }

  @Test
  public void itBuildsSetTagForMultipleDeferred() {
    Map<String, String> deferredValuesToSet = ImmutableMap.of("foo", "'bar'", "baz", "2");
    String result = EagerTagDecorator.buildSetTagForDeferredInChildContext(
      deferredValuesToSet,
      interpreter,
      true
    );
    assertThat(result).isEqualTo("{% set foo,baz = 'bar',2 %}");
    assertThat(context.getEagerTokens()).hasSize(1);
    EagerToken eagerToken = context
      .getEagerTokens()
      .stream()
      .findAny()
      .orElseThrow(RuntimeException::new);
    assertThat(eagerToken.getSetDeferredWords()).containsExactlyInAnyOrder("foo", "baz");
    assertThat(eagerToken.getUsedDeferredWords()).isEmpty();
  }

  @Test
  public void itWrapsInRawTag() {
    String toWrap = "{{ foo }}";
    JinjavaConfig preserveRawConfig = JinjavaConfig
      .newBuilder()
      .withExecutionMode(new PreserveRawExecutionMode())
      .build();
    assertThat(
        EagerTagDecorator.wrapInRawIfNeeded(
          toWrap,
          new JinjavaInterpreter(jinjava, context, preserveRawConfig)
        )
      )
      .isEqualTo(String.format("{%% raw %%}%s{%% endraw %%}", toWrap));
  }

  @Test
  public void itDoesntWrapInRawTagUnnecessarily() {
    String toWrap = "foo";
    JinjavaConfig preserveRawConfig = JinjavaConfig
      .newBuilder()
      .withExecutionMode(new PreserveRawExecutionMode())
      .build();
    assertThat(
        EagerTagDecorator.wrapInRawIfNeeded(
          toWrap,
          new JinjavaInterpreter(jinjava, context, preserveRawConfig)
        )
      )
      .isEqualTo(toWrap);
  }

  @Test
  public void itDoesntWrapInRawTagForDefaultConfig() {
    String toWrap = "{{ foo }}";
    assertThat(EagerTagDecorator.wrapInRawIfNeeded(toWrap, interpreter))
      .isEqualTo(toWrap);
  }

  @Test
  public void itWrapsInAutoEscapeTag() {
    String toWrap = "<div>{{deferred}}</div>";
    interpreter.getContext().setAutoEscape(true);
    assertThat(EagerTagDecorator.wrapInAutoEscapeIfNeeded(toWrap, interpreter))
      .isEqualTo(String.format("{%% autoescape %%}%s{%% endautoescape %%}", toWrap));
  }

  @Test
  public void itDoesntWrapInAutoEscapeWhenFalse() {
    String toWrap = "<div>{{deferred}}</div>";
    interpreter.getContext().setAutoEscape(false);
    assertThat(EagerTagDecorator.wrapInAutoEscapeIfNeeded(toWrap, interpreter))
      .isEqualTo(toWrap);
  }

  @Test
  public void itReconstructsTheEndOfATagNode() {
    TagNode tagNode = getMockTagNode("endif");
    assertThat(EagerTagDecorator.reconstructEnd(tagNode)).isEqualTo("{% endif %}");
    tagNode = getMockTagNode("endfor");
    assertThat(EagerTagDecorator.reconstructEnd(tagNode)).isEqualTo("{% endfor %}");
  }

  private static TagNode getMockTagNode(String endName) {
    TagNode mockTagNode = mock(TagNode.class);
    when(mockTagNode.getSymbols()).thenReturn(new DefaultTokenScannerSymbols());
    when(mockTagNode.getEndName()).thenReturn(endName);
    return mockTagNode;
  }
}