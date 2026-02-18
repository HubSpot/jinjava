package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.LazyExpression;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.lib.tag.eager.EagerExecutionResult;
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.mode.PreserveRawExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EagerReconstructionUtilsTest extends BaseInterpretingTest {

  private static final long MAX_OUTPUT_SIZE = 50L;

  @Before
  public void eagerSetup() throws Exception {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        BaseJinjavaTest
          .newConfigBuilder()
          .withMaxOutputSize(MAX_OUTPUT_SIZE)
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );

    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itExecutesInChildContextAndTakesNewValue() {
    context.put("foo", new PyList(new ArrayList<>()));
    EagerExecutionResult result = EagerContextWatcher.executeInChildContext(
      (interpreter1 -> {
          ((List<Integer>) interpreter1.getContext().get("foo")).add(1);
          return EagerExpressionResult.fromString("function return");
        }),
      interpreter,
      EagerContextWatcher.EagerChildContextConfig
        .newBuilder()
        .withTakeNewValue(true)
        .withForceDeferredExecutionMode(true)
        .withCheckForContextChanges(true)
        .build()
    );

    assertThat(context.get("foo")).isEqualTo(ImmutableList.of(1));
    assertThat(context.getDeferredTokens()).isEmpty();
    assertThat(result.getResult().toString()).isEqualTo("function return");
    // This will add an eager token because we normally don't call this method
    // unless we're in deferred execution mode.
    assertThat(result.getPrefixToPreserveState().toString())
      .isEqualTo("{% set foo = [1] %}");
  }

  @Test
  public void itExecutesInChildContextAndDefersNewValue() {
    context.put("foo", new ArrayList<Integer>());
    EagerExecutionResult result = EagerContextWatcher.executeInChildContext(
      (interpreter1 -> {
          context.put(
            "foo",
            DeferredValue.instance(interpreter1.getContext().get("foo"))
          );
          return EagerExpressionResult.fromString("function return");
        }),
      interpreter,
      EagerContextWatcher.EagerChildContextConfig
        .newBuilder()
        .withForceDeferredExecutionMode(true)
        .withCheckForContextChanges(true)
        .build()
    );
    assertThat(result.getResult().toString()).isEqualTo("function return");
    assertThat(result.getPrefixToPreserveState().toString())
      .isEqualTo("{% set foo = [] %}");
    assertThat(context.get("foo")).isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itReconstructsMacroFunctionsFromGlobal() {
    Set<String> deferredWords = new HashSet<>();
    deferredWords.add("foo");
    String image = "{% macro foo(bar) %}something{% endmacro %}";
    MacroFunction mockMacroFunction = getMockMacroFunction(image);
    context.addGlobalMacro(mockMacroFunction);
    String result = EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
      deferredWords,
      interpreter
    );
    assertThat(result).isEqualTo(image);
    assertThat(deferredWords).isEmpty();
  }

  @Test
  public void itReconstructsMacroFunctionsFromLocal() {
    Set<String> deferredWords = new HashSet<>();
    deferredWords.add("local.foo");
    String image = "{% macro foo(bar) %}something{% endmacro %}";
    MacroFunction mockMacroFunction = getMockMacroFunction(image);
    Map<String, Object> localAlias = new PyMap(ImmutableMap.of("foo", mockMacroFunction));
    context.put("local", localAlias);
    String result = EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
      deferredWords,
      interpreter
    );
    assertThat(result).isEqualTo("{% macro local.foo(bar) %}something{% endmacro %}");
    assertThat(deferredWords).isEmpty();
  }

  @Test
  public void itReconstructsVariables() {
    Set<String> deferredWords = new HashSet<>();
    deferredWords.add("foo.append");
    context.put("foo", new PyList(new ArrayList<>()));
    String result = EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
      deferredWords,
      interpreter
    );
    assertThat(result).isEqualTo("{% set foo = [] %}");
  }

  @Test
  public void itDoesntReconstructVariablesInDeferredExecutionMode() {
    Set<String> deferredWords = new HashSet<>();
    deferredWords.add("foo.append");
    context.put("foo", new PyList(new ArrayList<>()));
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      interpreter.getContext().setDeferredExecutionMode(true);
      String result = EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
        deferredWords,
        interpreter
      );
      assertThat(result).isEqualTo("");
    }
  }

  @Test
  public void itReconstructsVariablesAndMacroFunctions() {
    Set<String> deferredWords = new HashSet<>();
    deferredWords.add("bar.append");
    deferredWords.add("foo");
    String image = "{% macro foo(bar) %}something{% endmacro %}";
    MacroFunction mockMacroFunction = getMockMacroFunction(image);
    context.addGlobalMacro(mockMacroFunction);
    context.put("bar", new PyList(new ArrayList<>()));
    String result = EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
      deferredWords,
      interpreter
    );
    assertThat(result)
      .isEqualTo("{% macro foo(bar) %}something{% endmacro %}{% set bar = [] %}");
  }

  @Test
  public void itBuildsSetTagForDeferredAndRegisters() {
    String result =
      EagerReconstructionUtils.buildBlockOrInlineSetTagAndRegisterDeferredToken(
        "foo",
        "bar",
        interpreter
      );
    assertThat(result).isEqualTo("{% set foo = 'bar' %}");
    assertThat(context.getDeferredTokens()).hasSize(1);
    DeferredToken deferredToken = context
      .getDeferredTokens()
      .stream()
      .findAny()
      .orElseThrow(RuntimeException::new);
    assertThat(deferredToken.getSetDeferredWords()).containsExactly("foo");
    assertThat(deferredToken.getUsedDeferredWords()).isEmpty();
  }

  @Test
  public void itBuildsSetTagForDeferredAndDoesntRegister() {
    String result = EagerReconstructionUtils.buildBlockOrInlineSetTag(
      "foo",
      "bar",
      interpreter
    );
    assertThat(result).isEqualTo("{% set foo = 'bar' %}");
    assertThat(context.getDeferredTokens()).isEmpty();
  }

  @Test
  public void itBuildsSetTagForMultipleDeferred() {
    Map<String, String> deferredValuesToSet = ImmutableMap.of("foo", "'bar'", "baz", "2");
    String result = EagerReconstructionUtils.buildSetTag(
      deferredValuesToSet,
      interpreter,
      true
    );
    assertThat(result).isEqualTo("{% set foo,baz = 'bar',2 %}");
    assertThat(context.getDeferredTokens()).hasSize(1);
    DeferredToken deferredToken = context
      .getDeferredTokens()
      .stream()
      .findAny()
      .orElseThrow(RuntimeException::new);
    assertThat(deferredToken.getSetDeferredWords())
      .containsExactlyInAnyOrder("foo", "baz");
    assertThat(deferredToken.getUsedDeferredWords()).isEmpty();
  }

  @Test
  public void itLimitsSetTagConstruction() {
    StringBuilder tooLong = new StringBuilder();
    for (int i = 0; i < MAX_OUTPUT_SIZE; i++) {
      tooLong.append(i);
    }
    assertThatThrownBy(() ->
        EagerReconstructionUtils.buildBlockOrInlineSetTagAndRegisterDeferredToken(
          "foo",
          tooLong.toString(),
          interpreter
        )
      )
      .isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itWrapsInRawTag() {
    String toWrap = "{{ foo }}";
    JinjavaConfig preserveRawConfig = BaseJinjavaTest
      .newConfigBuilder()
      .withExecutionMode(PreserveRawExecutionMode.instance())
      .build();
    assertThat(
      EagerReconstructionUtils.wrapInRawIfNeeded(
        toWrap,
        new JinjavaInterpreter(jinjava, context, preserveRawConfig)
      )
    )
      .isEqualTo(String.format("{%% raw %%}%s{%% endraw %%}", toWrap));
  }

  @Test
  public void itDoesntWrapInRawTagUnnecessarily() {
    String toWrap = "foo";
    JinjavaConfig preserveRawConfig = BaseJinjavaTest
      .newConfigBuilder()
      .withExecutionMode(PreserveRawExecutionMode.instance())
      .build();
    assertThat(
      EagerReconstructionUtils.wrapInRawIfNeeded(
        toWrap,
        new JinjavaInterpreter(jinjava, context, preserveRawConfig)
      )
    )
      .isEqualTo(toWrap);
  }

  @Test
  public void itDoesntWrapInRawTagForDefaultConfig() {
    JinjavaConfig defaultConfig = BaseJinjavaTest
      .newConfigBuilder()
      .withExecutionMode(DefaultExecutionMode.instance())
      .build();
    String toWrap = "{{ foo }}";
    assertThat(
      EagerReconstructionUtils.wrapInRawIfNeeded(
        toWrap,
        new JinjavaInterpreter(jinjava, context, defaultConfig)
      )
    )
      .isEqualTo(toWrap);
  }

  @Test
  public void itWrapsInAutoEscapeTag() {
    String toWrap = "<div>{{deferred}}</div>";
    interpreter.getContext().setAutoEscape(true);
    assertThat(EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(toWrap, interpreter))
      .isEqualTo(String.format("{%% autoescape %%}%s{%% endautoescape %%}", toWrap));
  }

  @Test
  public void itDoesntWrapInAutoEscapeWhenFalse() {
    String toWrap = "<div>{{deferred}}</div>";
    interpreter.getContext().setAutoEscape(false);
    assertThat(EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(toWrap, interpreter))
      .isEqualTo(toWrap);
  }

  @Test
  public void itReconstructsTheEndOfATagNode() {
    TagNode tagNode = getMockTagNode("endif");
    assertThat(EagerReconstructionUtils.reconstructEnd(tagNode)).isEqualTo("{% endif %}");
    tagNode = getMockTagNode("endfor");
    assertThat(EagerReconstructionUtils.reconstructEnd(tagNode))
      .isEqualTo("{% endfor %}");
  }

  @Test
  public void itIgnoresMetaContextVariables() {
    interpreter
      .getContext()
      .put(Context.IMPORT_RESOURCE_ALIAS_KEY, DeferredValue.instance());
    assertThat(
      EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
        Collections.singleton(Context.IMPORT_RESOURCE_ALIAS_KEY),
        interpreter
      )
    )
      .isEmpty();
  }

  @Test
  public void itDiscardsSessionBindings() {
    interpreter.getContext().put("foo", "bar");
    EagerExecutionResult withSessionBindings = EagerContextWatcher.executeInChildContext(
      eagerInterpreter -> {
        interpreter.getContext().put("foo", "foobar");
        return EagerExpressionResult.fromString("");
      },
      interpreter,
      EagerContextWatcher.EagerChildContextConfig
        .newBuilder()
        .withDiscardSessionBindings(false)
        .withCheckForContextChanges(true)
        .build()
    );
    EagerExecutionResult withoutSessionBindings =
      EagerContextWatcher.executeInChildContext(
        eagerInterpreter -> {
          interpreter.getContext().put("foo", "foobar");
          return EagerExpressionResult.fromString("");
        },
        interpreter,
        EagerContextWatcher.EagerChildContextConfig
          .newBuilder()
          .withDiscardSessionBindings(true)
          .withCheckForContextChanges(true)
          .build()
      );
    assertThat(withSessionBindings.getSpeculativeBindings())
      .containsEntry("foo", "foobar");
    assertThat(withoutSessionBindings.getSpeculativeBindings()).doesNotContainKey("foo");
  }

  @Test
  public void itDoesNotBreakOnNullLazyExpressions() {
    interpreter.getContext().put("foo", LazyExpression.of(() -> null, ""));
    EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResult.fromString(interpreter.render("{% set foo = 'bar' %}")),
      interpreter,
      EagerContextWatcher.EagerChildContextConfig
        .newBuilder()
        .withDiscardSessionBindings(false)
        .withCheckForContextChanges(true)
        .withTakeNewValue(true)
        .build()
    );
  }

  private EagerMacroFunction getMockMacroFunction(String image) {
    interpreter.render(image);
    return (EagerMacroFunction) interpreter.getContext().getGlobalMacro("foo");
  }

  private static TagNode getMockTagNode(String endName) {
    TagNode mockTagNode = mock(TagNode.class);
    when(mockTagNode.getSymbols()).thenReturn(new DefaultTokenScannerSymbols());
    when(mockTagNode.getEndName()).thenReturn(endName);
    return mockTagNode;
  }
}
