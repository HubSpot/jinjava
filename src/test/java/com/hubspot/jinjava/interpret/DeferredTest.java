package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class DeferredTest {

  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    Jinjava jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
    interpreter.getContext().put("deferred", DeferredValue.instance());
    interpreter.getContext().put("resolved", "resolvedValue");
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
    String output = interpreter.render("{% if deferred %}{{resolved}}{% else %}b{% endif %}");
    assertThat(output).isEqualTo("{% if deferred %}{{resolved}}{% else %}b{% endif %}");
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
  public void itPreservesForTag() {
    String output = interpreter.render("{% for item in deferred %}{{item.name}}{% else %}last{% endfor %}");
    assertThat(output).isEqualTo("{% for item in deferred %}{{item.name}}{% else %}last{% endfor %}");
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

}
