package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.mode.PreserveRawExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for the static methods in the EagerTagDecorator.
 */
@RunWith(MockitoJUnitRunner.class)
public class EagerTagDecoratorTest extends BaseInterpretingTest {
  private static final long MAX_OUTPUT_SIZE = 50L;
  private Tag mockTag;
  private EagerGenericTag<Tag> eagerTagDecorator;

  @Before
  public void eagerSetup() throws Exception {
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "add_to_context",
          this.getClass().getDeclaredMethod("addToContext", String.class, Object.class)
        )
      );
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "modify_context",
          this.getClass().getDeclaredMethod("modifyContext", String.class, Object.class)
        )
      );
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withMaxOutputSize(MAX_OUTPUT_SIZE)
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    mockTag = mock(Tag.class);
    eagerTagDecorator = new EagerGenericTag<>(mockTag);

    JinjavaInterpreter.pushCurrent(interpreter);
    context.put("deferred", DeferredValue.instance());
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itExecutesInChildContextAndTakesNewValue() {
    context.put("foo", new PyList(new ArrayList<>()));
    EagerExecutionResult result = EagerTagDecorator.executeInChildContext(
      (
        interpreter1 -> {
          ((List<Integer>) interpreter1.getContext().get("foo")).add(1);
          return EagerExpressionResult.fromString("function return");
        }
      ),
      interpreter,
      true,
      false,
      true
    );

    assertThat(context.get("foo")).isEqualTo(ImmutableList.of(1));
    assertThat(context.getEagerTokens()).isEmpty();
    assertThat(result.getResult().toString()).isEqualTo("function return");
    // This will add an eager token because we normally don't call this method
    // unless we're in deferred execution mode.
    assertThat(result.getPrefixToPreserveState()).isEqualTo("{% set foo = [1] %}");
  }

  @Test
  public void itExecutesInChildContextAndDefersNewValue() {
    context.put("foo", new ArrayList<Integer>());
    EagerExecutionResult result = EagerTagDecorator.executeInChildContext(
      (
        interpreter1 -> {
          context.put(
            "foo",
            DeferredValue.instance(interpreter1.getContext().get("foo"))
          );
          return EagerExpressionResult.fromString("function return");
        }
      ),
      interpreter,
      false,
      false,
      true
    );
    assertThat(result.getResult().toString()).isEqualTo("function return");
    assertThat(result.getPrefixToPreserveState()).isEqualTo("{% set foo = [] %}");
    assertThat(context.get("foo")).isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itReconstructsMacroFunctionsFromGlobal() {
    Set<String> deferredWords = new HashSet<>();
    deferredWords.add("foo");
    String image = "{% macro foo(bar) %}something{% endmacro %}";
    MacroFunction mockMacroFunction = getMockMacroFunction(image);
    context.addGlobalMacro(mockMacroFunction);
    String result = EagerTagDecorator.reconstructFromContextBeforeDeferring(
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
    String result = EagerTagDecorator.reconstructFromContextBeforeDeferring(
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
    String result = EagerTagDecorator.reconstructFromContextBeforeDeferring(
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
    context.setDeferredExecutionMode(true);
    String result = EagerTagDecorator.reconstructFromContextBeforeDeferring(
      deferredWords,
      interpreter
    );
    assertThat(result).isEqualTo("");
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
    String result = EagerTagDecorator.reconstructFromContextBeforeDeferring(
      deferredWords,
      interpreter
    );
    assertThat(result)
      .isEqualTo("{% macro foo(bar) %}something{% endmacro %}{% set bar = [] %}");
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
  public void itLimitsSetTagConstruction() {
    StringBuilder tooLong = new StringBuilder();
    for (int i = 0; i < MAX_OUTPUT_SIZE; i++) {
      tooLong.append(i);
    }
    Map<String, String> deferredValuesToSet = ImmutableMap.of("foo", tooLong.toString());
    assertThatThrownBy(
        () ->
          EagerTagDecorator.buildSetTagForDeferredInChildContext(
            deferredValuesToSet,
            interpreter,
            true
          )
      )
      .isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itWrapsInRawTag() {
    String toWrap = "{{ foo }}";
    JinjavaConfig preserveRawConfig = JinjavaConfig
      .newBuilder()
      .withExecutionMode(PreserveRawExecutionMode.instance())
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
      .withExecutionMode(PreserveRawExecutionMode.instance())
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
    JinjavaConfig defaultConfig = JinjavaConfig
      .newBuilder()
      .withExecutionMode(DefaultExecutionMode.instance())
      .build();
    String toWrap = "{{ foo }}";
    assertThat(
        EagerTagDecorator.wrapInRawIfNeeded(
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

  @Test
  public void itDoesntLimitShortString() {
    String template = "{% if true %}abc{% endif %}";

    TagNode tagNode = (TagNode) (interpreter.parse(template).getChildren().get(0));
    assertThat(eagerTagDecorator.eagerInterpret(tagNode, interpreter))
      .isEqualTo(template);
    assertThat(interpreter.getErrors()).hasSize(0);
  }

  @Test
  public void itLimitsEagerInterpretLength() {
    StringBuilder tooLong = new StringBuilder();
    for (int i = 0; i < MAX_OUTPUT_SIZE; i++) {
      tooLong.append(i);
    }
    TagNode tagNode = (TagNode) (
      interpreter
        .parse(String.format("{%% raw %%}%s{%% endraw %%}", tooLong.toString()))
        .getChildren()
        .get(0)
    );
    assertThatThrownBy(() -> eagerTagDecorator.eagerInterpret(tagNode, interpreter))
      .isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itLimitsInterpretLength() {
    when(mockTag.interpret(any(), any())).thenThrow(new DeferredValueException(""));
    StringBuilder tooLong = new StringBuilder();
    for (int i = 0; i < MAX_OUTPUT_SIZE; i++) {
      tooLong.append(i);
    }
    TagNode tagNode = (TagNode) (
      interpreter
        .parse(String.format("{%% raw %%}%s{%% endraw %%}", tooLong.toString()))
        .getChildren()
        .get(0)
    );
    assertThatThrownBy(() -> eagerTagDecorator.interpret(tagNode, interpreter))
      .isInstanceOf(DeferredValueException.class);
    assertThat(interpreter.getErrors()).hasSize(1);
    assertThat(interpreter.getErrors().get(0).getReason())
      .isEqualTo(ErrorReason.OUTPUT_TOO_BIG);
  }

  @Test
  public void itLimitsTagLength() {
    TagNode tagNode = (TagNode) (
      interpreter.parse("{% print range(0, 50) %}").getChildren().get(0)
    );
    assertThatThrownBy(
        () ->
          eagerTagDecorator.getEagerTagImage((TagToken) tagNode.getMaster(), interpreter)
      )
      .isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itPutsOnContextInChildContext() {
    assertThat(interpreter.render("{{ add_to_context('foo', 'bar') }}{{ foo }}"))
      .isEqualTo("bar");
  }

  @Test
  public void itModifiesContextInChildContext() {
    context.put("foo", new ArrayList<>());
    assertThat(interpreter.render("{{ modify_context('foo', 'bar') }}{{ foo }}"))
      .isEqualTo("[bar]");
  }

  @Test
  public void itDoesntModifyContextWhenResultIsDeferred() {
    context.put("foo", new ArrayList<>());
    assertThat(
        interpreter.render("{{ modify_context('foo', 'bar') ~ deferred }}{{ foo }}")
      )
      .isEqualTo("{{ null ~ deferred }}[bar]");
  }

  private static MacroFunction getMockMacroFunction(String image) {
    MacroFunction mockMacroFunction = mock(MacroFunction.class);
    when(mockMacroFunction.getName()).thenReturn("foo");
    when(mockMacroFunction.getArguments()).thenReturn(ImmutableList.of("bar"));
    when(mockMacroFunction.getEvaluationResult(anyMap(), anyMap(), anyList(), any()))
      .thenReturn(image.substring(image.indexOf("%}") + 2, image.lastIndexOf("{%")));
    return mockMacroFunction;
  }

  private static TagNode getMockTagNode(String endName) {
    TagNode mockTagNode = mock(TagNode.class);
    when(mockTagNode.getSymbols()).thenReturn(new DefaultTokenScannerSymbols());
    when(mockTagNode.getEndName()).thenReturn(endName);
    return mockTagNode;
  }

  public static void addToContext(String key, Object value) {
    JinjavaInterpreter.getCurrent().getContext().put(key, value);
  }

  public static void modifyContext(String key, Object value) {
    ((List<Object>) JinjavaInterpreter.getCurrent().getContext().get(key)).add(value);
  }
}
