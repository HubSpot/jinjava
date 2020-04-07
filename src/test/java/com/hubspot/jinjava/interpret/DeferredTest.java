package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Joiner;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeferredTest {
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    Jinjava jinjava = new Jinjava();

    Context context = new Context();
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .build();
    interpreter = new JinjavaInterpreter(jinjava, context, config);
    interpreter.getContext().put("deferred", DeferredValue.instance());
    interpreter.getContext().put("resolved", "resolvedValue");
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
    assertThat(output).isEqualTo("{% if deferred %}{{resolved}}{% else %}b{% endif %}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesNestedIfTag() {
    String output = interpreter.render(
      "{% if deferred %}{% if resolved %}{{resolved}}{% endif %}{% else %}b{% endif %}"
    );
    assertThat(output)
      .isEqualTo(
        "{% if deferred %}{% if resolved %}{{resolved}}{% endif %}{% else %}b{% endif %}"
      );
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
  public void itResolvesForTagWherePossible() {
    String output = interpreter.render(
      "{% for i in [1, 2] %}{{i}}{{deferred}}{% endfor %}"
    );
    assertThat(output).isEqualTo("1{{deferred}}2{{deferred}}");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesNestedExpressions() {
    interpreter.getContext().put("nested", "some {{deferred}} value");
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
    interpreter.getContext().put("padding", 0);
    interpreter.getContext().put("added_padding", 10);
    String deferredOutput = interpreter.render(
      "{% macro inc_padding(width) %}" +
      "{% set padding = padding + width %}" +
      "{{padding}}" +
      "{% endmacro %}" +
      "{{ padding }}," +
      "{% set padding =  inc_padding(added_padding) | int %}" +
      "{{ padding }}," +
      "{% set padding = inc_padding(deferred) | int %}" +
      "{{ padding}}," +
      "{% set padding = inc_padding(added_padding) | int %}" +
      "{{ padding }}"
    );
    Object padding = interpreter.getContext().get("padding");
    assertThat(padding).isInstanceOf(DeferredValue.class);
    assertThat(((DeferredValue) padding).getOriginalValue()).isEqualTo(10);

    interpreter.getContext().put("padding", ((DeferredValue) padding).getOriginalValue());
    interpreter.getContext().put("added_padding", 10);
    // not deferred anymore
    interpreter.getContext().put("deferred", 5);
    interpreter.getContext().getGlobalMacro("inc_padding").setDeferred(false);

    String output = interpreter.render(deferredOutput);
    assertThat(output).isEqualTo("0,10,15,25");
  }

  @Test
  public void itPreservesVariablesUsedInDeferredBlock() {
    String template = "";
    template += "{% for item in resolved %}";
    template += "{% set varUsedInForScope = 'outside if statement' %}";
    template += "   {% if deferred %}";
    template += "     {{ varUsedInForScope }}";
    template += "     {% set varUsedInForScope = 'entered if statement' %}";
    template += "   {% endif %}";
    template += "   {{ varUsedInForScope }}";
    template += "{% endfor %}";

    String desiredDeferredOutput =
      "   {% if deferred %}     {{ varUsedInForScope }}     {% set varUsedInForScope = 'entered if statement' %}   {% endif %}   {{ varUsedInForScope }}";
    String output = interpreter.render(template);
    //No Scope Copying:
    //Interpreter Scope after render:
    // deferred=com.hubspot.jinjava.interpret.DeferredValue@1af2d44a
    // resolved=resolvedValue
    //Note that varUsedInForScope is lost
    //Output 1st render: {% if deferred %}     {{ varUsedInForScope }}     {% set varUsedInForScope = 'entered if statement' %}   {% endif %}   outside if statement
    //Output 2nd render:                   outside if statement
    //Note the missing first print statement from inside the deferred statement. The printed statement is simply preserved from the 1st render and should have the value "entered if statement"

    //With Scope Copying:
    // resolved=resolvedValue
    // loop=com.hubspot.jinjava.util.ForLoop@c88a337
    // varUsedInForScope=outside if statement
    // item=resolvedValue
    // deferred=com.hubspot.jinjava.interpret.DeferredValue@5d0a1059
    //Output 1st render: {% if deferred %}     {{ varUsedInForScope }}     {% set varUsedInForScope = 'entered if statement' %}   {% endif %}   outside if statement
    //Output 2nd render:         outside if statement           outside if statement
    //This result is more functional as it allows the var in the if block to be evaluated but it does not have the expected new value for varUsedInForScope

    Joiner.MapJoiner mapJoiner = Joiner.on(",").withKeyValueSeparator("=");

    System.out.println(mapJoiner.join(interpreter.getContext()));
    //assertThat(output).isEqualTo(desiredDeferredOutput);

    interpreter.getContext().put("deferred", "resolved");
    String outputWithNoDeferredValues = interpreter.render(output);
    String expectedOutputWithNoDeferred = "outside if statement    entered if statement";
    assertThat(outputWithNoDeferredValues).isEqualTo(expectedOutputWithNoDeferred);
  }
}
