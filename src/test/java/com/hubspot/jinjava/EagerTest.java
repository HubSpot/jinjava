package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.mode.ExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.util.DeferredValueUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EagerTest {

  private JinjavaInterpreter interpreter;
  private Jinjava jinjava;
  private ExpectedTemplateInterpreter expectedTemplateInterpreter;
  Context globalContext = new Context();
  Context localContext; // ref to context created with global as parent

  @Before
  public void setup() {
    setupWithExecutionMode(EagerExecutionMode.instance());
  }

  protected void setupWithExecutionMode(ExecutionMode executionMode) {
    JinjavaInterpreter.popCurrent();
    jinjava = new Jinjava();
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private RelativePathResolver relativePathResolver = new RelativePathResolver();

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
          return Resources.toString(
            Resources.getResource(fullName),
            StandardCharsets.UTF_8
          );
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .withExecutionMode(executionMode)
      .withNestedInterpretationEnabled(true)
      .withLegacyOverrides(
        LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
      )
      .withMaxMacroRecursionDepth(20)
      .withEnableRecursiveMacroCalls(true)
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      globalContext,
      config
    );
    interpreter = new JinjavaInterpreter(parentInterpreter);
    expectedTemplateInterpreter =
      ExpectedTemplateInterpreter.withSensibleCurrentPath(jinjava, interpreter, "eager");
    localContext = interpreter.getContext();

    localContext.put("deferred", DeferredValue.instance());
    localContext.put("resolved", "resolvedValue");
    localContext.put("dict", ImmutableSet.of("a", "b", "c"));
    localContext.put("dict2", ImmutableSet.of("e", "f", "g"));
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    try {
      assertThat(interpreter.getErrors()).isEmpty();
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itReconstructsMapWithNullValues() {
    interpreter.render("{% set foo = {'foo': null} %}");
    assertThat(interpreter.getContext().get("foo")).isInstanceOf(Map.class);
    assertThat((Map) interpreter.getContext().get("foo")).hasSize(1);
  }

  @Test
  public void itDefersNodeWhenModifiedInForLoop() {
    assertThat(
      interpreter.render(
        "{% set bar = 'bar' %}{% set foo = 0 %}{% for i in deferred %}{{ bar ~ foo ~ bar }} {% set foo = foo + 1 %}{% endfor %}"
      )
    )
      .isEqualTo(
        "{% set foo = 0 %}{% for i in deferred %}{{ 'bar' ~ foo ~ 'bar' }} {% set foo = foo + 1 %}{% endfor %}"
      );
  }

  @Test
  public void checkAssumptions() {
    // Just checking assumptions
    String output = interpreter.render("deferred");
    assertThat(output).isEqualTo("deferred");

    output = interpreter.render("resolved");
    assertThat(output).isEqualTo("resolved");

    output = interpreter.render("a {{resolved}} b");
    assertThat(output).isEqualTo("a resolvedValue b");
    assertThat(interpreter.getErrors()).isEmpty();

    assertThat(localContext.getParent()).isEqualTo(globalContext);
  }

  @Test
  public void itDefersSimpleExpressions() {
    String output = interpreter.render("a {{ deferred }} b");
    assertThat(output).isEqualTo("a {{ deferred }} b");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersWholeNestedExpressions() {
    String output = interpreter.render("a {{ deferred.nested }} b");
    assertThat(output).isEqualTo("a {{ deferred.nested }} b");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersAsLittleAsPossible() {
    String output = interpreter.render("a {{ deferred }} {{resolved}} b");
    assertThat(output).isEqualTo("a {{ deferred }} resolvedValue b");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesIfTag() {
    String output = interpreter.render(
      "{% if deferred %}{{resolved}}{% else %}b{% endif %}"
    );
    assertThat(output).isEqualTo("{% if deferred %}resolvedValue{% else %}b{% endif %}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itEagerlyResolvesNestedIfTag() {
    String output = interpreter.render(
      "{% if deferred %}{% if resolved %}{{resolved}}{% endif %}{% else %}b{% endif %}"
    );
    assertThat(output).isEqualTo("{% if deferred %}resolvedValue{% else %}b{% endif %}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  /**
   * This may or may not be desirable behaviour.
   */
  @Test
  public void itDoesntPreservesElseIfTag() {
    String output = interpreter.render("{% if true %}a{% elif deferred %}b{% endif %}");
    assertThat(output).isEqualTo("a");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itResolvesIfTagWherePossible() {
    String output = interpreter.render("{% if true %}{{ deferred }}{% endif %}");
    assertThat(output).isEqualTo("{{ deferred }}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itResolveEqualToInOrCondition() {
    String output = interpreter.render(
      "{% if 'a' is equalto 'b' or 'a' is equalto 'a' %}{{ deferred }}{% endif %}"
    );
    assertThat(output).isEqualTo("{{ deferred }}");
  }

  @Test
  public void itPreserveDeferredVariableResolvingEqualToInOrCondition() {
    String inputOutputExpected =
      "{% if 'a' is equalto 'b' or 'a' is equalto deferred %}preserved{% endif %}";
    String output = interpreter.render(inputOutputExpected);

    assertThat(output)
      .isEqualTo(
        "{% if false || exptest:equalto.evaluate('a', ____int3rpr3t3r____, deferred) %}preserved{% endif %}"
      );
    assertThat(interpreter.getErrors()).isEmpty();
    localContext.put("deferred", "a");
    assertThat(interpreter.render(output)).isEqualTo("preserved");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itDoesNotResolveForTagDeferredBlockInside() {
    String output = interpreter.render(
      "{% for item in dict %}{% if item == deferred %} equal {% else %} not equal {% endif %}{% endfor %}"
    );
    StringBuilder expected = new StringBuilder();
    expected.append("{% for __ignored__ in [0] %}");
    for (String item : (Set<String>) localContext.get("dict")) {
      expected
        .append(String.format("{%% if '%s' == deferred %%}", item))
        .append(" equal {% else %} not equal {% endif %}");
    }
    expected.append("{% endfor %}");
    assertThat(output).isEqualTo(expected.toString());
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itDoesNotResolveForTagDeferredBlockNestedInside() {
    String output = interpreter.render(
      "{% for item in dict %}{% if item == 'a' %} equal {% if item == deferred %}{% endif %}{% else %} not equal {% endif %}{% endfor %}"
    );
    StringBuilder expected = new StringBuilder();
    expected.append("{% for __ignored__ in [0] %}");
    for (String item : (Set<String>) localContext.get("dict")) {
      if (item.equals("a")) {
        expected.append(" equal {% if 'a' == deferred %}{% endif %}");
      } else {
        expected.append(" not equal ");
      }
    }
    expected.append("{% endfor %}");
    assertThat(output).isEqualTo(expected.toString());
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itDoesNotResolveNestedForTags() {
    String output = interpreter.render(
      "{% for item in dict %}{% for item2 in dict2 %}{% if item2 == 'e' %} equal {% if item2 == deferred %}{% endif %}{% else %} not equal {% endif %}{% endfor %}{% endfor %}"
    );

    StringBuilder expected = new StringBuilder();
    expected.append("{% for __ignored__ in [0] %}");
    for (String item : (Set<String>) localContext.get("dict")) {
      expected.append("{% for __ignored__ in [0] %}");
      for (String item2 : (Set<String>) localContext.get("dict2")) {
        if (item2.equals("e")) {
          expected.append(" equal {% if 'e' == deferred %}{% endif %}");
        } else {
          expected.append(" not equal ");
        }
      }
      expected.append("{% endfor %}");
    }
    expected.append("{% endfor %}");
    assertThat(output).isEqualTo(expected.toString());
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesNestedExpressions() {
    localContext.put("nested", "some {{ deferred }} value");
    String output = interpreter.render("Test {{nested}}");
    assertThat(output).isEqualTo("Test some {{ deferred }} value");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesForTag() {
    String output = interpreter.render(
      "{% for item in deferred %}{{ item.name }}last{% endfor %}"
    );
    assertThat(output)
      .isEqualTo("{% for item in deferred %}{{ item.name }}last{% endfor %}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesFilters() {
    String output = interpreter.render("{{ deferred|capitalize }}");
    assertThat(output)
      .isEqualTo("{{ filter:capitalize.filter(deferred, ____int3rpr3t3r____) }}");
    assertThat(interpreter.getErrors()).isEmpty();
    localContext.put("deferred", "foo");
    assertThat(interpreter.render(output)).isEqualTo("Foo");
  }

  @Test
  public void itPreservesFunctions() {
    String output = interpreter.render("{{ deferred|datetimeformat('%B %e, %Y') }}");
    assertThat(output)
      .isEqualTo(
        "{{ filter:datetimeformat.filter(deferred, ____int3rpr3t3r____, '%B %e, %Y') }}"
      );
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesRandomness() {
    String output = interpreter.render("{{ [1, 2, 3]|shuffle }}");
    assertThat(output)
      .isEqualTo("{{ filter:shuffle.filter([1, 2, 3], ____int3rpr3t3r____) }}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersMacro() {
    localContext.put("padding", 0);
    localContext.put("added_padding", 10);
    String deferredOutput = interpreter.render(
      expectedTemplateInterpreter.getDeferredFixtureTemplate("deferred-macro.jinja")
    );
    Object padding = localContext.get("padding");
    assertThat(padding).isInstanceOf(DeferredValue.class);
    assertThat(((DeferredValue) padding).getOriginalValue()).isEqualTo(10);

    localContext.put("padding", ((DeferredValue) padding).getOriginalValue());
    localContext.put("added_padding", 10);
    // not deferred anymore
    localContext.put("deferred", 5);
    localContext.remove("int");
    localContext.getGlobalMacro("inc_padding").setDeferred(false);

    String output = interpreter.render(deferredOutput);
    assertThat(output.replace("\n", "")).isEqualTo("0,10,15,25");
  }

  @Test
  public void itDefersAllVariablesUsedInDeferredNode() {
    String template = expectedTemplateInterpreter.getDeferredFixtureTemplate(
      "vars-in-deferred-node.jinja"
    );
    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    String output = interpreter.render(template);
    Object varInScope = localContext.get("varUsedInForScope");
    assertThat(varInScope).isInstanceOf(DeferredValue.class);
    DeferredValue varInScopeDeferred = (DeferredValue) varInScope;
    assertThat(varInScopeDeferred.getOriginalValue()).isEqualTo("outside if statement");

    HashMap<String, Object> deferredContext =
      DeferredValueUtils.getDeferredContextWithOriginalValues(localContext);
    deferredContext.forEach(localContext::put);
    String secondRender = interpreter.render(output);
    assertThat(secondRender).isEqualTo("outside if statement entered if statement");

    localContext.put("deferred", DeferredValue.instance());
    localContext.put("resolved", "resolvedValue");
  }

  @Test
  public void itDefersDependantVariables() {
    String template = "";
    template +=
    "{% set resolved_variable = 'resolved' %} {% set deferred_variable = deferred + '-' + resolved_variable %}";
    template += "{{ deferred_variable }}";
    interpreter.render(template);
    localContext.get("resolved_variable");
  }

  @Test
  public void itDefersVariablesComparedAgainstDeferredVals() {
    String template = "";
    template += "{% set testVar = 'testvalue' %}";
    template += "{% if deferred == testVar %} true {% else %} false {% endif %}";

    localContext.put("deferred", DeferredValue.instance("testvalue"));
    String output = interpreter.render(template);
    assertThat(output.trim())
      .isEqualTo("{% if deferred == 'testvalue' %} true {% else %} false {% endif %}");

    HashMap<String, Object> deferredContext =
      DeferredValueUtils.getDeferredContextWithOriginalValues(localContext);
    deferredContext.forEach(localContext::put);
    String secondRender = interpreter.render(output);
    assertThat(secondRender.trim()).isEqualTo("true");
  }

  @Test
  public void itDoesNotPutDeferredVariablesOnGlobalContext() {
    String template = expectedTemplateInterpreter.getDeferredFixtureTemplate(
      "set-within-lower-scope.jinja"
    );
    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    interpreter.render(template);
    assertThat(globalContext).isEmpty();
  }

  @Test
  public void itPutsDeferredVariablesOnParentScopes() {
    String template = expectedTemplateInterpreter.getDeferredFixtureTemplate(
      "set-within-lower-scope.jinja"
    );
    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    String output = interpreter.render(template);
    HashMap<String, Object> deferredContext =
      DeferredValueUtils.getDeferredContextWithOriginalValues(localContext);
    deferredContext.forEach(localContext::put);
    String secondRender = interpreter.render(output);
    assertThat(secondRender.trim()).isEqualTo("inside first scope".trim());
  }

  @Test
  public void puttingDeferredVariablesOnParentScopesDoesNotBreakSetTag() {
    String template = expectedTemplateInterpreter.getDeferredFixtureTemplate(
      "set-within-lower-scope-twice.jinja"
    );

    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    String output = interpreter.render(template);

    HashMap<String, Object> deferredContext =
      DeferredValueUtils.getDeferredContextWithOriginalValues(localContext);
    deferredContext.forEach(localContext::put);
    String secondRender = interpreter.render(output);
    assertThat(secondRender.trim())
      .isEqualTo("inside first scopeinside first scope2".trim());
  }

  @Test
  public void itMarksVariablesSetInDeferredBlockAsDeferred() {
    String template = expectedTemplateInterpreter.getDeferredFixtureTemplate(
      "set-in-deferred.jinja"
    );

    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    String output = interpreter.render(template);
    Context context = localContext;
    assertThat(localContext).containsKey("varSetInside");
    Object varSetInside = localContext.get("varSetInside");
    assertThat(varSetInside).isInstanceOf(DeferredValue.class);
    assertThat(output).contains("{{ varSetInside }}").contains("xyz");
    assertThat(context.get("a")).isInstanceOf(DeferredValue.class);
    assertThat(context.get("b")).isInstanceOf(DeferredValue.class);
    assertThat(context.get("c")).isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itMarksVariablesUsedAsMapKeysAsDeferred() {
    String template = expectedTemplateInterpreter.getDeferredFixtureTemplate(
      "deferred-map-access.jinja"
    );

    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    localContext.put("deferredValue2", DeferredValue.instance("key"));
    ImmutableMap<String, ImmutableMap<String, String>> map = ImmutableMap.of(
      "map",
      ImmutableMap.of("key", "value")
    );
    localContext.put("imported", map);

    String output = interpreter.render(template);
    assertThat(localContext).containsKey("deferredValue2");
    Object deferredValue2 = localContext.get("deferredValue2");
    localContext
      .getDeferredNodes()
      .forEach(node ->
        DeferredValueUtils.findAndMarkDeferredProperties(localContext, node)
      );
    assertThat(deferredValue2).isInstanceOf(DeferredValue.class);
    assertThat(output)
      .contains(
        "{% set varSetInside = {'key': 'value'} [deferredValue2.nonexistentprop] %}"
      );
  }

  @Test
  public void itEagerlyDefersSet() {
    localContext.put("bar", true);
    expectedTemplateInterpreter.assertExpectedOutput("eagerly-defers-set/test");
  }

  @Test
  public void itEvaluatesNonEagerSet() {
    expectedTemplateInterpreter.assertExpectedOutput("evaluates-non-eager-set/test");
    assertThat(
      localContext
        .getDeferredTokens()
        .stream()
        .flatMap(deferredToken -> deferredToken.getSetDeferredWords().stream())
        .collect(Collectors.toSet())
    )
      .isEmpty();
    assertThat(
      localContext
        .getDeferredTokens()
        .stream()
        .flatMap(deferredToken -> deferredToken.getUsedDeferredWords().stream())
        .collect(Collectors.toSet())
    )
      .contains("deferred");
  }

  @Test
  public void itDefersOnImmutableMode() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-on-immutable-mode/test");
  }

  @Test
  public void itDoesntAffectParentFromEagerIf() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "doesnt-affect-parent-from-eager-if/test"
    );
  }

  @Test
  public void itDefersEagerChildScopedVars() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-eager-child-scoped-vars/test"
    );
  }

  @Test
  public void itSetsMultipleVarsDeferredInChild() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "sets-multiple-vars-deferred-in-child/test"
    );
  }

  @Test
  public void itSetsMultipleVarsDeferredInChildSecondPass() {
    localContext.put("deferred", true);
    expectedTemplateInterpreter.assertExpectedOutput(
      "sets-multiple-vars-deferred-in-child/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "sets-multiple-vars-deferred-in-child/test.expected"
    );
  }

  @Test
  public void itDoesntDoubleAppendInDeferredIfTag() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "doesnt-double-append-in-deferred-if-tag/test"
    );
  }

  @Test
  public void itPrependsSetIfStateChanges() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "prepends-set-if-state-changes/test"
    );
  }

  @Test
  public void itHandlesLoopVarAgainstDeferredInLoop() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-loop-var-against-deferred-in-loop/test"
    );
  }

  @Test
  public void itHandlesLoopVarAgainstDeferredInLoopSecondPass() {
    localContext.put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-loop-var-against-deferred-in-loop/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-loop-var-against-deferred-in-loop/test.expected"
    );
  }

  @Test
  public void itDefersMacroForDoAndPrint() {
    localContext.put("my_list", new PyList(new ArrayList<>()));
    localContext.put("first", 10);
    localContext.put("deferred2", DeferredValue.instance());
    String deferredOutput = expectedTemplateInterpreter.assertExpectedOutput(
      "defers-macro-for-do-and-print/test"
    );
    Object myList = localContext.get("my_list");
    assertThat(myList).isInstanceOf(DeferredValue.class);
    assertThat(((DeferredValue) myList).getOriginalValue())
      .isEqualTo(ImmutableList.of(10L));

    localContext.put("my_list", ((DeferredValue) myList).getOriginalValue());
    localContext.put("first", 10);
    // not deferred anymore
    localContext.put("deferred", 5);
    localContext.put("deferred2", 10);

    // TODO auto remove deferred
    localContext.getDeferredTokens().clear();
    localContext.getGlobalMacro("macro_append").setDeferred(false);

    String output = interpreter.render(deferredOutput);
    assertThat(output.replace("\n", ""))
      .isEqualTo("Is ([]),Macro: [10]Is ([10]),Is ([10, 5]),Macro: [10, 5, 10]");
  }

  @Test
  public void itDefersMacroInFor() {
    localContext.put("my_list", new PyList(new ArrayList<>()));
    expectedTemplateInterpreter.assertExpectedOutput("defers-macro-in-for/test");
  }

  @Test
  public void itDefersMacroInIf() {
    localContext.put("my_list", new PyList(new ArrayList<>()));
    expectedTemplateInterpreter.assertExpectedOutput("defers-macro-in-if/test");
  }

  @Test
  public void itPutsDeferredImportedMacroInOutput() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "puts-deferred-imported-macro-in-output/test"
    );
  }

  @Test
  public void itPutsDeferredImportedMacroInOutputSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "puts-deferred-imported-macro-in-output/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "puts-deferred-imported-macro-in-output/test.expected"
    );
  }

  @Test
  public void itPutsDeferredFromedMacroInOutput() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "puts-deferred-fromed-macro-in-output/test"
    );
  }

  @Test
  public void itEagerlyDefersMacro() {
    localContext.put("foo", "I am foo");
    localContext.put("bar", "I am bar");
    expectedTemplateInterpreter.assertExpectedOutput("eagerly-defers-macro/test");
  }

  @Test
  public void itEagerlyDefersMacroSecondPass() {
    localContext.put("foo", "I am foo");
    localContext.put("bar", "I am bar");
    localContext.put("deferred", true);
    expectedTemplateInterpreter.assertExpectedOutput(
      "eagerly-defers-macro/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "eagerly-defers-macro/test.expected"
    );
  }

  @Test
  public void itLoadsImportedMacroSyntax() {
    expectedTemplateInterpreter.assertExpectedOutput("loads-imported-macro-syntax/test");
  }

  @Test
  public void itDefersCaller() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-caller/test");
  }

  @Test
  public void itDefersCallerSecondPass() {
    localContext.put("deferred", "foo");
    expectedTemplateInterpreter.assertExpectedOutput("defers-caller/test.expected");
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "defers-caller/test.expected"
    );
  }

  @Test
  public void itDefersMacroInExpression() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-macro-in-expression/test");
  }

  @Test
  public void itDefersMacroInExpressionSecondPass() {
    interpreter.resolveELExpression("(range(0,1))", -1);
    localContext.put("deferred", 5);
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-macro-in-expression/test.expected"
    );
    localContext.put("deferred", 5);
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "defers-macro-in-expression/test.expected"
    );
  }

  @Test
  public void itHandlesDeferredInIfchanged() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-in-ifchanged/test"
    );
  }

  @Test
  public void itDefersIfchanged() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-ifchanged/test");
  }

  @Test
  public void itHandlesCycleInDeferredFor() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-cycle-in-deferred-for/test"
    );
  }

  @Test
  public void itHandlesCycleInDeferredForSecondPass() {
    localContext.put("deferred", new String[] { "foo", "bar", "foobar", "baz" });
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-cycle-in-deferred-for/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-cycle-in-deferred-for/test.expected"
    );
  }

  @Test
  public void itHandlesDeferredInCycle() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-deferred-in-cycle/test");
  }

  @Test
  public void itHandlesDeferredCycleAs() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-deferred-cycle-as/test");
  }

  @Test
  public void itHandlesDeferredCycleAsSecondPass() {
    localContext.put("deferred", "hello");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-cycle-as/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-deferred-cycle-as/test.expected"
    );
  }

  @Test
  public void itHandlesNonDeferringCycles() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-non-deferring-cycles/test");
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-non-deferring-cycles/test"
    );
  }

  @Test
  public void itHandlesAutoEscape() {
    localContext.put("myvar", "foo < bar");
    expectedTemplateInterpreter.assertExpectedOutput("handles-auto-escape/test");
  }

  @Test
  public void itWrapsCertainOutputInRaw() {
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .withExecutionMode(EagerExecutionMode.instance())
      .withNestedInterpretationEnabled(false)
      .withLegacyOverrides(
        LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
      )
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      globalContext,
      config
    );
    JinjavaInterpreter noNestedInterpreter = new JinjavaInterpreter(parentInterpreter);

    JinjavaInterpreter.pushCurrent(noNestedInterpreter);
    try {
      ExpectedTemplateInterpreter
        .withSensibleCurrentPath(jinjava, noNestedInterpreter, "eager")
        .assertExpectedOutput("wraps-certain-output-in-raw/test");
      assertThat(noNestedInterpreter.getErrors()).isEmpty();
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itHandlesDeferredImportVars() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-deferred-import-vars/test"
    );
  }

  @Test
  public void itHandlesDeferredImportVarsSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-import-vars/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-deferred-import-vars/test.expected"
    );
  }

  @Test
  public void itHandlesNonDeferredImportVars() {
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-non-deferred-import-vars/test"
    );
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-non-deferred-import-vars/test"
    );
  }

  @Test
  public void itHandlesDeferredFromImportAs() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-from-import-as/test"
    );
  }

  @Test
  public void itHandlesDeferredFromImportAsSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-from-import-as/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-deferred-from-import-as/test.expected"
    );
  }

  @Test
  public void itPreservesValueSetInIf() {
    expectedTemplateInterpreter.assertExpectedOutput("preserves-value-set-in-if/test");
  }

  @Test
  public void itHandlesCycleWithQuote() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-cycle-with-quote/test");
  }

  @Test
  public void itHandlesUnknownFunctionErrors() {
    JinjavaInterpreter eagerInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContextCopy(),
      JinjavaConfig.newBuilder().withExecutionMode(EagerExecutionMode.instance()).build()
    );
    JinjavaInterpreter defaultInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContextCopy(),
      JinjavaConfig
        .newBuilder()
        .withExecutionMode(DefaultExecutionMode.instance())
        .build()
    );
    try {
      JinjavaInterpreter.pushCurrent(eagerInterpreter);
      ExpectedTemplateInterpreter
        .withSensibleCurrentPath(jinjava, eagerInterpreter, "eager")
        .assertExpectedOutput("handles-unknown-function-errors/test");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
    try {
      JinjavaInterpreter.pushCurrent(defaultInterpreter);

      ExpectedTemplateInterpreter
        .withSensibleCurrentPath(jinjava, defaultInterpreter, "eager")
        .assertExpectedOutput("handles-unknown-function-errors/test");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
    assertThat(eagerInterpreter.getErrors()).hasSize(2);
    assertThat(defaultInterpreter.getErrors()).hasSize(2);
  }

  @Test
  public void itKeepsMaxMacroRecursionDepth() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "keeps-max-macro-recursion-depth/test"
    );
  }

  @Test
  public void itHandlesComplexRaw() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-complex-raw/test");
  }

  @Test
  public void itHandlesDeferredInNamespace() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-in-namespace/test"
    );
  }

  @Test
  public void itHandlesDeferredInNamespaceSecondPass() {
    localContext.put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-in-namespace/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-deferred-in-namespace/test.expected"
    );
  }

  @Test
  public void itHandlesClashingNameInMacro() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-clashing-name-in-macro/test"
    );
  }

  @Test
  public void itHandlesClashingNameInMacroSecondPass() {
    localContext.put("deferred", 0);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-clashing-name-in-macro/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-clashing-name-in-macro/test.expected"
    );
  }

  @Test
  public void itHandlesBlockSetInDeferredIf() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-block-set-in-deferred-if/test"
    );
  }

  @Test
  public void itHandlesBlockSetInDeferredIfSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-block-set-in-deferred-if/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-block-set-in-deferred-if/test.expected"
    );
  }

  @Test
  public void itDoesntOverwriteElif() {
    expectedTemplateInterpreter.assertExpectedOutput("doesnt-overwrite-elif/test");
  }

  @Test
  public void itHandlesSetAndModifiedInFor() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-set-and-modified-in-for/test"
    );
  }

  @Test
  public void itHandlesSetInFor() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-set-in-for/test");
  }

  @Test
  public void itHandlesDeferringLoopVariable() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferring-loop-variable/test"
    );
  }

  @Test
  public void itDefersChangesWithinDeferredSetBlock() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "defers-changes-within-deferred-set-block/test"
    );
  }

  @Test
  public void itDefersChangesWithinDeferredSetBlockSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-changes-within-deferred-set-block/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "defers-changes-within-deferred-set-block/test.expected"
    );
  }

  @Test
  public void itHandlesImportWithMacrosInDeferredIf() {
    String template = expectedTemplateInterpreter.getFixtureTemplate(
      "handles-import-with-macros-in-deferred-if/test"
    );
    JinjavaInterpreter.getCurrent().render(template);
    assertThat(JinjavaInterpreter.getCurrent().getContext().getDeferredNodes())
      .isNotEmpty();
  }

  @Test
  public void itHandlesImportInDeferredIf() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-import-in-deferred-if/test"
    );
  }

  @Test
  public void itAllowsMetaContextVarOverriding() {
    interpreter.getContext().addMetaContextVariables(Collections.singleton("meta"));
    interpreter.getContext().put("meta", "META");
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "allows-meta-context-var-overriding/test"
    );
  }

  @Test
  public void itHandlesValueModifiedInMacro() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-value-modified-in-macro/test"
    );
  }

  @Test
  public void itHandlesDoubleImportModification() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-double-import-modification/test"
    );
  }

  @Test
  public void itHandlesDoubleImportModificationSecondPass() {
    interpreter.getContext().put("deferred", false);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-double-import-modification/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-double-import-modification/test.expected"
    );
  }

  @Test
  public void itHandlesSameNameImportVar() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-same-name-import-var/test"
    );
  }

  @Test
  public void itHandlesSameNameImportVarSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-same-name-import-var/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-same-name-import-var/test.expected"
    );
  }

  @Test
  public void itReconstructsTypesProperly() {
    expectedTemplateInterpreter.assertExpectedOutput("reconstructs-types-properly/test");
  }

  @Test
  public void itRunsForLoopInsideDeferredForLoop() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "runs-for-loop-inside-deferred-for-loop/test"
    );
  }

  @Test
  public void itModifiesVariableInDeferredMacro() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "modifies-variable-in-deferred-macro/test"
    );
  }

  @Test
  public void itRevertsSimple() {
    expectedTemplateInterpreter.assertExpectedOutput("reverts-simple/test");
  }

  @Test
  public void itScopesResettingBindings() {
    expectedTemplateInterpreter.assertExpectedOutput("scopes-resetting-bindings/test");
  }

  @Test
  public void itReconstructsWithMultipleLoops() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-with-multiple-loops/test"
    );
  }

  @Test
  public void itFullyDefersFilteredMacro() {
    // Macro and set tag reconstruction are flipped so not exactly idempotent, but functionally identical
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "fully-defers-filtered-macro/test"
    );
  }

  @Test
  public void itFullyDefersFilteredMacroSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "fully-defers-filtered-macro/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "fully-defers-filtered-macro/test.expected"
    );
  }

  @Test
  public void itDefersLargeLoop() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-large-loop/test");
  }

  @Test
  public void itHandlesSetInInnerScope() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-set-in-inner-scope/test");
  }

  @Test
  public void itCorrectlyDefersWithMultipleLoops() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "correctly-defers-with-multiple-loops/test"
    );
  }

  @Test
  public void itRevertsModificationWithDeferredLoop() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "reverts-modification-with-deferred-loop/test"
    );
  }

  @Test
  public void itReconstructsMapNode() {
    expectedTemplateInterpreter.assertExpectedOutput("reconstructs-map-node/test");
  }

  @Test
  public void itReconstructsMapNodeSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-map-node/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "reconstructs-map-node/test.expected"
    );
  }

  @Test
  public void itHasProperLineStripping() {
    expectedTemplateInterpreter.assertExpectedOutput("has-proper-line-stripping/test");
  }

  @Test
  public void itDefersCallTagWithDeferredArgument() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-call-tag-with-deferred-argument/test"
    );
  }

  @Test
  public void itDefersCallTagWithDeferredArgumentSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-call-tag-with-deferred-argument/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "defers-call-tag-with-deferred-argument/test.expected"
    );
  }

  @Test
  public void itHandlesDuplicateVariableReferenceModification() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-duplicate-variable-reference-modification/test"
    );
  }

  @Test
  public void itHandlesDuplicateVariableReferenceSpeculativeModification() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-duplicate-variable-reference-speculative-modification/test"
    );
  }

  @Test
  public void itHandlesHigherScopeReferenceModification() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-higher-scope-reference-modification/test"
    );
  }

  @Test
  public void itHandlesHigherScopeReferenceModificationSecondPass() {
    interpreter.getContext().put("deferred", "b");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-higher-scope-reference-modification/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-higher-scope-reference-modification/test.expected"
    );
  }

  @Test
  public void itHandlesReferenceModificationWhenSourceIsLost() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-reference-modification-when-source-is-lost/test"
    );
  }

  @Test
  public void itDoesNotReferentialDeferForSetVars() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "does-not-referential-defer-for-set-vars/test"
    );
  }

  @Test
  public void itKeepsScopeIsolationFromForLoops() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "keeps-scope-isolation-from-for-loops/test"
    );
  }

  @Test
  public void itDoesNotOverrideImportModificationInFor() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "does-not-override-import-modification-in-for/test"
    );
  }

  @Test
  public void itDoesNotOverrideImportModificationInForSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "does-not-override-import-modification-in-for/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "does-not-override-import-modification-in-for/test.expected"
    );
  }

  @Test
  public void itHandlesDeferredForLoopVarFromMacro() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-deferred-for-loop-var-from-macro/test"
    );
  }

  @Test
  public void itHandlesDeferredForLoopVarFromMacroSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-for-loop-var-from-macro/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-deferred-for-loop-var-from-macro/test.expected"
    );
  }

  @Test
  public void itReconstructsBlockSetVariablesInForLoop() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-block-set-variables-in-for-loop/test"
    );
  }

  @Test
  public void itReconstructsNullVariablesInDeferredCaller() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "reconstructs-null-variables-in-deferred-caller/test"
    );
  }

  @Test
  public void itReconstructsNamespaceForSetTagsUsingPeriod() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-namespace-for-set-tags-using-period/test"
    );
  }

  @Test
  public void itReconstructsNamespaceForSetTagsUsingPeriodSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-namespace-for-set-tags-using-period/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "reconstructs-namespace-for-set-tags-using-period/test.expected"
    );
  }

  @Test
  public void itUsesUniqueMacroNames() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "uses-unique-macro-names/test"
    );
  }

  @Test
  public void itUsesUniqueMacroNamesSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "uses-unique-macro-names/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "uses-unique-macro-names/test.expected"
    );
  }

  @Test
  public void itReconstructsWordsFromInsideNestedExpressions() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-words-from-inside-nested-expressions/test"
    );
  }

  @Test
  public void itReconstructsWordsFromInsideNestedExpressionsSecondPass() {
    interpreter.getContext().put("deferred", new PyList(new ArrayList<>()));
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "reconstructs-words-from-inside-nested-expressions/test.expected"
    );
  }

  @Test
  public void itDoesNotReconstructExtraTimes() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "does-not-reconstruct-extra-times/test"
    );
  }

  @Test
  public void itAllowsModificationInResolvedForLoop() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "allows-modification-in-resolved-for-loop/test"
    );
  }

  @Test
  @Ignore // Test isn't necessary after https://github.com/HubSpot/jinjava/pull/988 got reverted
  public void itOnlyDefersLoopItemOnCurrentContext() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "only-defers-loop-item-on-current-context/test"
    );
  }

  @Test
  public void itRunsMacroFunctionInDeferredExecutionMode() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "runs-macro-function-in-deferred-execution-mode/test"
    );
  }

  @Test
  public void itKeepsMacroModificationsInScope() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "keeps-macro-modifications-in-scope/test"
    );
  }

  @Test
  public void itKeepsMacroModificationsInScopeSecondPass() {
    interpreter.getContext().put("deferred", true);
    expectedTemplateInterpreter.assertExpectedOutput(
      "keeps-macro-modifications-in-scope/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "keeps-macro-modifications-in-scope/test.expected"
    );
  }

  @Test
  public void itDoesNotReconstructVariableInWrongScope() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "does-not-reconstruct-variable-in-wrong-scope/test"
    );
  }

  @Test
  public void itDoesNotReconstructVariableInWrongScopeSecondPass() {
    interpreter.getContext().put("deferred", true);
    expectedTemplateInterpreter.assertExpectedOutput(
      "does-not-reconstruct-variable-in-wrong-scope/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "does-not-reconstruct-variable-in-wrong-scope/test.expected"
    );
  }

  @Test
  public void itReconstructsDeferredVariableEventually() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-deferred-variable-eventually/test"
    );
  }

  @Test
  public void itDoesntDoubleAppendInDeferredSet() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "doesnt-double-append-in-deferred-set/test"
    );
  }

  @Test
  public void itDoesntDoubleAppendInDeferredMacro() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "doesnt-double-append-in-deferred-macro/test"
    );
  }

  @Test
  public void itDoesNotReconstructVariableInSetInWrongScope() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "does-not-reconstruct-variable-in-set-in-wrong-scope/test"
    );
  }

  @Test
  public void itRreconstructsValueUsedInDeferredImportedMacro() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "reconstructs-value-used-in-deferred-imported-macro/test"
    );
  }

  @Test
  public void itRreconstructsValueUsedInDeferredImportedMacroSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-value-used-in-deferred-imported-macro/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "reconstructs-value-used-in-deferred-imported-macro/test.expected"
    );
  }

  @Test
  public void itAllowsDeferredLazyReferenceToGetOverridden() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "allows-deferred-lazy-reference-to-get-overridden/test"
    );
  }

  @Test
  public void itAllowsDeferredLazyReferenceToGetOverriddenSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "allows-deferred-lazy-reference-to-get-overridden/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "allows-deferred-lazy-reference-to-get-overridden/test.expected"
    );
  }

  @Test
  public void itCommitsVariablesFromDoTagWhenPartiallyResolved() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "commits-variables-from-do-tag-when-partially-resolved/test"
    );
  }

  @Test
  public void itCommitsVariablesFromDoTagWhenPartiallyResolvedSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "commits-variables-from-do-tag-when-partially-resolved/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedOutput(
      "commits-variables-from-do-tag-when-partially-resolved/test.expected"
    );
  }

  @Test
  public void itFindsDeferredWordsInsideReconstructedString() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "finds-deferred-words-inside-reconstructed-string/test"
    );
  }

  @Test
  public void itReconstructsNestedValueInStringRepresentation() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-nested-value-in-string-representation/test"
    );
  }

  @Test
  public void itReconstructsNestedValueInStringRepresentationSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-nested-value-in-string-representation/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "reconstructs-nested-value-in-string-representation/test.expected"
    );
  }

  @Test
  public void itDefersLoopSettingMetaContextVar() {
    interpreter.getContext().addMetaContextVariables(Collections.singleton("content"));
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-loop-setting-meta-context-var/test"
    );
  }

  @Test
  public void itDefersLoopSettingMetaContextVarSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    interpreter.getContext().addMetaContextVariables(Collections.singleton("content"));
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-loop-setting-meta-context-var/test.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "defers-loop-setting-meta-context-var/test.expected"
    );
  }

  @Test
  public void itAllowsVariableSharingAliasName() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "allows-variable-sharing-alias-name/test"
    );
  }

  @Test
  public void itFailsOnModificationInAliasedMacro() {
    String input = expectedTemplateInterpreter.getFixtureTemplate(
      "fails-on-modification-in-aliased-macro/test"
    );
    interpreter.render(input);
    // We don't support this
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itHandlesDeferredModificationInCaller() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-modification-in-caller/test"
    );
  }

  @Test
  public void itHandlesDeferredModificationInCallerSecondPass() {
    interpreter.getContext().put("deferred", "c");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-modification-in-caller/test.expected"
    );
  }

  @Test
  public void itPreservesRawInsideDeferredSetBlock() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "preserves-raw-inside-deferred-set-block/test"
    );
  }

  @Test
  public void itReconstructsAliasedMacro() {
    expectedTemplateInterpreter.assertExpectedOutput("reconstructs-aliased-macro/test");
  }

  @Test
  public void itReconstructsAliasedMacroSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-aliased-macro/test.expected"
    );
  }

  @Test
  public void itReconstructsBlockPathWhenDeferred() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "reconstructs-block-path-when-deferred/test"
    );
  }

  @Test
  public void itReconstructsBlockPathWhenDeferredSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-block-path-when-deferred/test.expected"
    );
  }

  @Test
  public void itReconstructsBlockPathWhenDeferredNested() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "reconstructs-block-path-when-deferred-nested/test"
    );
  }

  @Test
  public void itReconstructsBlockPathWhenDeferredNestedSecondPass() {
    interpreter.getContext().put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "reconstructs-block-path-when-deferred-nested/test.expected"
    );
  }

  @Test
  public void itKeepsMetaContextVariablesThroughImport() {
    setupWithExecutionMode(
      new EagerExecutionMode() {
        @Override
        public void prepareContext(Context context) {
          super.prepareContext(context);
          context.addMetaContextVariables(Collections.singleton("meta"));
        }
      }
    );
    interpreter.getContext().put("meta", new ArrayList<>());
    expectedTemplateInterpreter.assertExpectedOutput(
      "keeps-meta-context-variables-through-import/test"
    );
  }

  @Test
  public void itWrapsMacroThatWouldChangeCurrentPathInChildScope() {
    interpreter
      .getContext()
      .put(RelativePathResolver.CURRENT_PATH_CONTEXT_KEY, "starting path");
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "wraps-macro-that-would-change-current-path-in-child-scope/test"
    );
  }

  @Test
  public void itHandlesDeferredBreakInForLoop() {
    String input = expectedTemplateInterpreter.getFixtureTemplate(
      "handles-deferred-break-in-for-loop/test"
    );
    interpreter.render(input);
    // We don't support this yet
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itHandlesDeferredContinueInForLoop() {
    String input = expectedTemplateInterpreter.getFixtureTemplate(
      "handles-deferred-continue-in-for-loop/test"
    );
    interpreter.render(input);
    // We don't support this yet
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }

  @Test
  public void itReconstructsFromedMacro() {
    expectedTemplateInterpreter.assertExpectedOutput("reconstructs-fromed-macro/test");
  }

  @Test
  public void itAllowsOverridingMetaContextVariableInChildScope() {
    interpreter.getContext().addMetaContextVariables(Collections.singleton("meta"));
    interpreter.getContext().put("meta", "META");
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "allows-overriding-meta-context-variable-in-child-scope/test"
    );
  }
}
