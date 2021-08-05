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
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.util.DeferredValueUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerTest {
  private JinjavaInterpreter interpreter;
  private final Jinjava jinjava = new Jinjava();
  private ExpectedTemplateInterpreter expectedTemplateInterpreter;
  Context globalContext = new Context();
  Context localContext; // ref to context created with global as parent

  @Before
  public void setup() {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private RelativePathResolver relativePathResolver = new RelativePathResolver();

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        )
          throws IOException {
          return Resources.toString(
            Resources.getResource(String.format("tags/macrotag/%s", fullName)),
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
      .withExecutionMode(EagerExecutionMode.instance())
      .withNestedInterpretationEnabled(true)
      .withLegacyOverrides(
        LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
      )
      .withMaxMacroRecursionDepth(5)
      .withEnableRecursiveMacroCalls(true)
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      globalContext,
      config
    );
    interpreter = new JinjavaInterpreter(parentInterpreter);
    expectedTemplateInterpreter =
      new ExpectedTemplateInterpreter(jinjava, interpreter, "eager");
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
    for (String item : (Set<String>) localContext.get("dict")) {
      expected
        .append(String.format("{%% if '%s' == deferred %%}", item))
        .append(" equal {% else %} not equal {% endif %}");
    }
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
    for (String item : (Set<String>) localContext.get("dict")) {
      if (item.equals("a")) {
        expected.append(" equal {% if 'a' == deferred %}{% endif %}");
      } else {
        expected.append(" not equal ");
      }
    }
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
    for (String item : (Set<String>) localContext.get("dict")) {
      for (String item2 : (Set<String>) localContext.get("dict2")) {
        if (item2.equals("e")) {
          expected.append(" equal {% if 'e' == deferred %}{% endif %}");
        } else {
          expected.append(" not equal ");
        }
      }
    }
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

    HashMap<String, Object> deferredContext = DeferredValueUtils.getDeferredContextWithOriginalValues(
      localContext
    );
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

    HashMap<String, Object> deferredContext = DeferredValueUtils.getDeferredContextWithOriginalValues(
      localContext
    );
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
    HashMap<String, Object> deferredContext = DeferredValueUtils.getDeferredContextWithOriginalValues(
      localContext
    );
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

    HashMap<String, Object> deferredContext = DeferredValueUtils.getDeferredContextWithOriginalValues(
      localContext
    );
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
    DeferredValueUtils.findAndMarkDeferredProperties(localContext);
    assertThat(deferredValue2).isInstanceOf(DeferredValue.class);
    assertThat(output)
      .contains(
        "{% set varSetInside = {'key': 'value'}[deferredValue2.nonexistentprop] %}"
      );
  }

  @Test
  public void itEagerlyDefersSet() {
    localContext.put("bar", true);
    expectedTemplateInterpreter.assertExpectedOutput("eagerly-defers-set");
  }

  @Test
  public void itEvaluatesNonEagerSet() {
    expectedTemplateInterpreter.assertExpectedOutput("evaluates-non-eager-set");
    assertThat(
        localContext
          .getEagerTokens()
          .stream()
          .flatMap(eagerToken -> eagerToken.getSetDeferredWords().stream())
          .collect(Collectors.toSet())
      )
      .containsExactlyInAnyOrder("item");
    assertThat(
        localContext
          .getEagerTokens()
          .stream()
          .flatMap(eagerToken -> eagerToken.getUsedDeferredWords().stream())
          .collect(Collectors.toSet())
      )
      .isEmpty();
  }

  @Test
  public void itDefersOnImmutableMode() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-on-immutable-mode");
  }

  @Test
  public void itDoesntAffectParentFromEagerIf() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "doesnt-affect-parent-from-eager-if"
    );
  }

  @Test
  public void itDefersEagerChildScopedVars() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-eager-child-scoped-vars");
  }

  @Test
  public void itSetsMultipleVarsDeferredInChild() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "sets-multiple-vars-deferred-in-child"
    );
  }

  @Test
  public void itSetsMultipleVarsDeferredInChildSecondPass() {
    localContext.put("deferred", true);
    expectedTemplateInterpreter.assertExpectedOutput(
      "sets-multiple-vars-deferred-in-child.expected"
    );
  }

  @Test
  public void itDoesntDoubleAppendInDeferredIfTag() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "doesnt-double-append-in-deferred-if-tag"
    );
  }

  @Test
  public void itPrependsSetIfStateChanges() {
    expectedTemplateInterpreter.assertExpectedOutput("prepends-set-if-state-changes");
  }

  @Test
  public void itHandlesLoopVarAgainstDeferredInLoop() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-loop-var-against-deferred-in-loop"
    );
  }

  @Test
  public void itHandlesLoopVarAgainstDeferredInLoopSecondPass() {
    localContext.put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-loop-var-against-deferred-in-loop.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-loop-var-against-deferred-in-loop.expected"
    );
  }

  @Test
  public void itDefersMacroForDoAndPrint() {
    localContext.put("my_list", new PyList(new ArrayList<>()));
    localContext.put("first", 10);
    localContext.put("deferred2", DeferredValue.instance());
    String deferredOutput = expectedTemplateInterpreter.assertExpectedOutput(
      "defers-macro-for-do-and-print"
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
    localContext.getEagerTokens().clear();
    localContext.getGlobalMacro("macro_append").setDeferred(false);

    String output = interpreter.render(deferredOutput);
    assertThat(output.replace("\n", ""))
      .isEqualTo("Is ([]),Macro: [10]Is ([10]),Is ([10, 5]),Macro: [10, 5, 10]");
  }

  @Test
  public void itDefersMacroInFor() {
    localContext.put("my_list", new PyList(new ArrayList<>()));
    expectedTemplateInterpreter.assertExpectedOutput("defers-macro-in-for");
  }

  @Test
  public void itDefersMacroInIf() {
    localContext.put("my_list", new PyList(new ArrayList<>()));
    expectedTemplateInterpreter.assertExpectedOutput("defers-macro-in-if");
  }

  @Test
  public void itPutsDeferredImportedMacroInOutput() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "puts-deferred-imported-macro-in-output"
    );
  }

  @Test
  public void itPutsDeferredImportedMacroInOutputSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "puts-deferred-imported-macro-in-output.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "puts-deferred-imported-macro-in-output.expected"
    );
  }

  @Test
  public void itPutsDeferredFromedMacroInOutput() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "puts-deferred-fromed-macro-in-output"
    );
  }

  @Test
  public void itEagerlyDefersMacro() {
    localContext.put("foo", "I am foo");
    localContext.put("bar", "I am bar");
    expectedTemplateInterpreter.assertExpectedOutput("eagerly-defers-macro");
  }

  @Test
  public void itEagerlyDefersMacroSecondPass() {
    localContext.put("deferred", true);
    expectedTemplateInterpreter.assertExpectedOutput("eagerly-defers-macro.expected");
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "eagerly-defers-macro.expected"
    );
  }

  @Test
  public void itLoadsImportedMacroSyntax() {
    expectedTemplateInterpreter.assertExpectedOutput("loads-imported-macro-syntax");
  }

  @Test
  public void itDefersCaller() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-caller");
  }

  @Test
  public void itDefersCallerSecondPass() {
    localContext.put("deferred", "foo");
    expectedTemplateInterpreter.assertExpectedOutput("defers-caller.expected");
    expectedTemplateInterpreter.assertExpectedNonEagerOutput("defers-caller.expected");
  }

  @Test
  public void itDefersMacroInExpression() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-macro-in-expression");
  }

  @Test
  public void itDefersMacroInExpressionSecondPass() {
    interpreter.resolveELExpression("(range(0,1))", -1);
    localContext.put("deferred", 5);
    expectedTemplateInterpreter.assertExpectedOutput(
      "defers-macro-in-expression.expected"
    );
    localContext.put("deferred", 5);
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "defers-macro-in-expression.expected"
    );
  }

  @Test
  public void itHandlesDeferredInIfchanged() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-deferred-in-ifchanged");
  }

  @Test
  public void itDefersIfchanged() {
    expectedTemplateInterpreter.assertExpectedOutput("defers-ifchanged");
  }

  @Test
  public void itHandlesCycleInDeferredFor() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-cycle-in-deferred-for");
  }

  @Test
  public void itHandlesCycleInDeferredForSecondPass() {
    localContext.put("deferred", new String[] { "foo", "bar", "foobar", "baz" });
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-cycle-in-deferred-for.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-cycle-in-deferred-for.expected"
    );
  }

  @Test
  public void itHandlesDeferredInCycle() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-deferred-in-cycle");
  }

  @Test
  public void itHandlesDeferredCycleAs() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-deferred-cycle-as");
  }

  @Test
  public void itHandlesDeferredCycleAsSecondPass() {
    localContext.put("deferred", "hello");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-cycle-as.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-deferred-cycle-as.expected"
    );
  }

  @Test
  public void itHandlesNonDeferringCycles() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-non-deferring-cycles");
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-non-deferring-cycles"
    );
  }

  @Test
  public void itHandlesAutoEscape() {
    localContext.put("myvar", "foo < bar");
    expectedTemplateInterpreter.assertExpectedOutput("handles-auto-escape");
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
      new ExpectedTemplateInterpreter(jinjava, noNestedInterpreter, "eager")
      .assertExpectedOutput("wraps-certain-output-in-raw");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itHandlesDeferredImportVars() {
    expectedTemplateInterpreter.assertExpectedOutputNonIdempotent(
      "handles-deferred-import-vars"
    );
  }

  @Test
  public void itHandlesDeferredImportVarsSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-import-vars.expected"
    );
  }

  @Test
  public void itHandlesNonDeferredImportVars() {
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-non-deferred-import-vars"
    );
    expectedTemplateInterpreter.assertExpectedOutput("handles-non-deferred-import-vars");
  }

  @Test
  public void itHandlesDeferredFromImportAs() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-deferred-from-import-as");
  }

  @Test
  public void itHandlesDeferredFromImportAsSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-from-import-as.expected"
    );
  }

  @Test
  public void itPreservesValueSetInIf() {
    expectedTemplateInterpreter.assertExpectedOutput("preserves-value-set-in-if");
  }

  @Test
  public void itHandlesCycleWithQuote() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-cycle-with-quote");
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
      new ExpectedTemplateInterpreter(jinjava, eagerInterpreter, "eager")
      .assertExpectedOutput("handles-unknown-function-errors");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
    try {
      JinjavaInterpreter.pushCurrent(defaultInterpreter);

      new ExpectedTemplateInterpreter(jinjava, defaultInterpreter, "eager")
      .assertExpectedOutput("handles-unknown-function-errors");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
    assertThat(eagerInterpreter.getErrors()).hasSize(2);
    assertThat(defaultInterpreter.getErrors()).hasSize(2);
  }

  @Test
  public void itKeepsMaxMacroRecursionDepth() {
    expectedTemplateInterpreter.assertExpectedOutput("keeps-max-macro-recursion-depth");
  }

  @Test
  public void itHandlesComplexRaw() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-complex-raw");
  }

  @Test
  public void itHandlesDeferredInNamespace() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-deferred-in-namespace");
  }

  @Test
  public void itHandlesDeferredInNamespaceSecondPass() {
    localContext.put("deferred", "resolved");
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-deferred-in-namespace.expected"
    );
  }

  @Test
  public void itHandlesClashingNameInMacro() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-clashing-name-in-macro");
  }

  @Test
  public void itHandlesClashingNameInMacroSecondPass() {
    localContext.put("deferred", 0);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-clashing-name-in-macro.expected"
    );
    expectedTemplateInterpreter.assertExpectedNonEagerOutput(
      "handles-clashing-name-in-macro.expected"
    );
  }

  @Test
  public void itHandlesBlockSetInDeferredIf() {
    expectedTemplateInterpreter.assertExpectedOutput("handles-block-set-in-deferred-if");
  }

  @Test
  public void itHandlesBlockSetInDeferredIfSecondPass() {
    localContext.put("deferred", 1);
    expectedTemplateInterpreter.assertExpectedOutput(
      "handles-block-set-in-deferred-if.expected"
    );
  }

  @Test
  public void itDoesntOverwriteElif() {
    expectedTemplateInterpreter.assertExpectedOutput("doesnt-overwrite-elif");
  }
}
