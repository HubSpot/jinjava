package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.lib.tag.EndTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.eager.EagerForTag;
import com.hubspot.jinjava.lib.tag.eager.EagerIfTag;
import com.hubspot.jinjava.lib.tag.eager.EagerTagFactory;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.util.DeferredValueUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerTest {
  private JinjavaInterpreter interpreter;
  private Jinjava jinjava = new Jinjava();
  private EagerTagFactory eagerTagFactory = new EagerTagFactory(
    EndTag.class,
    SetTag.class
  );
  Context globalContext = new Context();
  Context localContext; // ref to context created with global as parent

  @Before
  public void setup() {
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      globalContext,
      config
    );
    interpreter = new JinjavaInterpreter(parentInterpreter);
    localContext = interpreter.getContext();

    localContext
      .getAllTags()
      .stream()
      .map(tag -> eagerTagFactory.getEagerTagDecorator(tag.getClass()))
      .filter(Optional::isPresent)
      .forEach(maybeEagerTag -> localContext.registerTag(maybeEagerTag.get()));

    localContext.registerTag(new EagerIfTag());
    localContext.registerTag(new EagerForTag());
    localContext.put("deferred", DeferredValue.instance());
    localContext.put("resolved", "resolvedValue");
    localContext.put("dict", ImmutableSet.of("a", "b", "c"));
    localContext.put("dict2", ImmutableSet.of("e", "f", "g"));
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
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
    String output = interpreter.render("a {{deferred}} b");
    assertThat(output).isEqualTo("a {{deferred}} b");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersWholeNestedExpressions() {
    String output = interpreter.render("a {{deferred.nested}} b");
    assertThat(output).isEqualTo("a {{deferred.nested}} b");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersAsLittleAsPossible() {
    String output = interpreter.render("a {{deferred}} {{resolved}} b");
    assertThat(output).isEqualTo("a {{deferred}} resolvedValue b");
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
    String output = interpreter.render("{% if true %}{{deferred}}{% endif %}");
    assertThat(output).isEqualTo("{{deferred}}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itResolveEqualToInOrCondition() {
    String output = interpreter.render(
      "{% if 'a' is equalto 'b' or 'a' is equalto 'a' %}{{deferred}}{% endif %}"
    );
    assertThat(output).isEqualTo("{{deferred}}");
  }

  @Test
  public void itPreserveDeferredVariableResolvingEqualToInOrCondition() {
    String inputOutputExpected =
      "{% if 'a' is equalto 'b' or 'a' is equalto deferred %}preserved{% endif %}";
    String output = interpreter.render(inputOutputExpected);

    assertThat(output).isEqualTo(inputOutputExpected);
    assertThat(interpreter.getErrors()).isEmpty();
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
    localContext.put("nested", "some {{deferred}} value");
    String output = interpreter.render("Test {{nested}}");
    assertThat(output).isEqualTo("Test some {{deferred}} value");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesForTag() {
    String output = interpreter.render(
      "{% for item in deferred %}{{item.name}}{% else %}last{% endfor %}"
    );
    assertThat(output)
      .isEqualTo("{% for item in deferred %}{{item.name}}{% else %}last{% endfor %}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesFilters() {
    String output = interpreter.render("{{ deferred|capitalize }}");
    assertThat(output).isEqualTo("{{ deferred|capitalize }}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesFunctions() {
    String output = interpreter.render("{{ deferred|datetimeformat('%B %e, %Y') }}");
    assertThat(output).isEqualTo("{{ deferred|datetimeformat('%B %e, %Y') }}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesRandomness() {
    String output = interpreter.render("{{ [1,2,3]|shuffle }}");
    assertThat(output).isEqualTo("{{ [1,2,3]|shuffle }}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDefersMacro() {
    localContext.put("padding", 0);
    localContext.put("added_padding", 10);
    String deferredOutput = interpreter.render(
      getFixtureTemplate("deferred-macro.jinja")
    );
    Object padding = localContext.get("padding");
    assertThat(padding).isInstanceOf(DeferredValue.class);
    assertThat(((DeferredValue) padding).getOriginalValue()).isEqualTo(10);

    localContext.put("padding", ((DeferredValue) padding).getOriginalValue());
    localContext.put("added_padding", 10);
    // not deferred anymore
    localContext.put("deferred", 5);
    localContext.getGlobalMacro("inc_padding").setDeferred(false);

    String output = interpreter.render(deferredOutput);
    assertThat(output.replace("\n", "")).isEqualTo("0,10,15,25");
  }

  @Test
  public void itDefersAllVariablesUsedInDeferredNode() {
    String template = getFixtureTemplate("vars-in-deferred-node.jinja");
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

    //    Object varInScope = localContext.get("testVar");
    //    assertThat(varInScope).isInstanceOf(DeferredValue.class);
    //    DeferredValue varInScopeDeferred = (DeferredValue) varInScope;
    //    assertThat(varInScopeDeferred.getOriginalValue()).isEqualTo("testvalue");
    HashMap<String, Object> deferredContext = DeferredValueUtils.getDeferredContextWithOriginalValues(
      localContext
    );
    deferredContext.forEach(localContext::put);
    String secondRender = interpreter.render(output);
    assertThat(secondRender.trim()).isEqualTo("true");
  }

  @Test
  public void itDoesNotPutDeferredVariablesOnGlobalContext() {
    String template = getFixtureTemplate("set-within-lower-scope.jinja");
    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    interpreter.render(template);
    assertThat(globalContext).isEmpty();
  }

  @Test
  public void itPutsDeferredVariablesOnParentScopes() {
    String template = getFixtureTemplate("set-within-lower-scope.jinja");
    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    String output = interpreter.render(template);
    //    assertThat(localContext).containsKey("varSetInside");
    //    Object varSetInside = localContext.get("varSetInside");
    //    assertThat(varSetInside).isInstanceOf(DeferredValue.class);
    //    DeferredValue varSetInsideDeferred = (DeferredValue) varSetInside;
    //    assertThat(varSetInsideDeferred.getOriginalValue()).isEqualTo("inside first scope");
    HashMap<String, Object> deferredContext = DeferredValueUtils.getDeferredContextWithOriginalValues(
      localContext
    );
    deferredContext.forEach(localContext::put);
    String secondRender = interpreter.render(output);
    assertThat(secondRender.trim()).isEqualTo("inside first scope".trim());
  }

  @Test
  public void puttingDeferredVariablesOnParentScopesDoesNotBreakSetTag() {
    String template = getFixtureTemplate("set-within-lower-scope-twice.jinja");

    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    String output = interpreter.render(template);
    //    assertThat(localContext).containsKey("varSetInside");
    //    Object varSetInside = localContext.get("varSetInside");
    //    assertThat(varSetInside).isInstanceOf(DeferredValue.class);
    //    DeferredValue varSetInsideDeferred = (DeferredValue) varSetInside;
    //    assertThat(varSetInsideDeferred.getOriginalValue()).isEqualTo("inside first scope");

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
    String template = getFixtureTemplate("set-in-deferred.jinja");

    localContext.put("deferredValue", DeferredValue.instance("resolved"));
    String output = interpreter.render(template);
    Context context = localContext;
    assertThat(localContext).containsKey("varSetInside");
    Object varSetInside = localContext.get("varSetInside");
    assertThat(varSetInside).isInstanceOf(DeferredValue.class);
    assertThat(output).contains("{{ varSetInside }}");
    assertThat(context.get("a")).isInstanceOf(DeferredValue.class);
    assertThat(context.get("b")).isInstanceOf(DeferredValue.class);
    assertThat(context.get("c")).isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itMarksVariablesUsedAsMapKeysAsDeferred() {
    String template = getFixtureTemplate("deferred-map-access.jinja");

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
      .contains("{% set varSetInside = imported.map[deferredValue2.nonexistentprop] %}");
  }

  @Test
  public void itEagerlyDefersImport() {}

  @Test
  public void itEagerlyDefersMacro() {}

  @Test
  public void itEagerlyDefersFrom() {}

  private String getFixtureTemplate(String templateLocation) {
    try {
      try {
        return Resources.toString(
          Resources.getResource("deferred/" + templateLocation),
          Charsets.UTF_8
        );
      } catch (IllegalArgumentException e) {
        return Resources.toString(
          Resources.getResource("eager/" + templateLocation),
          Charsets.UTF_8
        );
      }
    } catch (IOException e) {
      return null;
    }
  }
}
