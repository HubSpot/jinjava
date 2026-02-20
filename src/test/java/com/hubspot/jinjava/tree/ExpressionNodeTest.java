package com.hubspot.jinjava.tree;

import static com.hubspot.jinjava.lib.expression.DefaultExpressionStrategy.ECHO_UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.features.BuiltInFeatures;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;

public class ExpressionNodeTest {

  protected JinjavaInterpreter nestedInterpreter;
  protected JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    nestedInterpreter =
      new Jinjava(
        BaseJinjavaTest.newConfigBuilder().withNestedInterpretationEnabled(true).build()
      )
        .newInterpreter();
    interpreter =
      new Jinjava(BaseJinjavaTest.newConfigBuilder().build()).newInterpreter();
  }

  @Test
  public void itRendersResultAsTemplateWhenContainingVarBlocks() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "hello {{ place }}");
      nestedInterpreter.getContext().put("place", "world");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(nestedInterpreter).toString()).isEqualTo("hello world");
    }
  }

  @Test
  public void itRendersResultWithNestedExpressionInterpretation() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "hello {{ place }}");
      nestedInterpreter.getContext().put("place", "world");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(nestedInterpreter).toString()).isEqualTo("hello world");
    }
  }

  @Test
  public void itRendersWithoutNestedExpressionInterpretationByDefault() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      interpreter.getContext().put("myvar", "hello {{ place }}");
      interpreter.getContext().put("place", "world");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(interpreter).toString()).isEqualTo("hello {{ place }}");
    }
  }

  @Test
  public void itRendersNestedTags() throws Exception {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withNestedInterpretationEnabled(true)
      .build();
    try (var a = JinjavaInterpreter.closeablePushCurrent(nestedInterpreter).get()) {
      nestedInterpreter
        .getContext()
        .put("myvar", "hello {% if (true) %}nasty{% endif %}");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(nestedInterpreter).toString()).isEqualTo("hello nasty");
    }
  }

  @Test
  public void itAvoidsInfiniteRecursionWhenVarsContainBraceBlocks() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      interpreter.getContext().put("myvar", "hello {{ place }}");
      interpreter.getContext().put("place", "{{ place }}");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(interpreter).toString()).isEqualTo("hello {{ place }}");
    }
  }

  @Test
  public void itAllowsNestedTagExpressions() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "{% if true %}{{ place }}{% endif %}");
      nestedInterpreter.getContext().put("place", "{% if true %}Hello{% endif %}");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(nestedInterpreter).toString()).isEqualTo("Hello");
    }
  }

  @Test
  public void itAvoidsInfiniteRecursionWhenVarsAreInIfBlocks() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "{% if true %}{{ place }}{% endif %}");
      nestedInterpreter.getContext().put("place", "{% if true %}{{ myvar }}{% endif %}");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(nestedInterpreter).toString())
        .isEqualTo("{% if true %}{{ myvar }}{% endif %}");
    }
  }

  @Test
  public void itDoesNotRescursivelyEvaluateExpressionsOfSelf() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "hello {{myvar}}");

      ExpressionNode node = fixture("simplevar");
      // It renders once, and then stop further rendering after detecting recursion.
      assertThat(node.render(nestedInterpreter).toString())
        .isEqualTo("hello hello {{myvar}}");
    }
  }

  @Test
  public void itDoesNotRescursivelyEvaluateExpressions() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "hello {{ place }}");
      nestedInterpreter.getContext().put("place", "{{location}}");
      nestedInterpreter.getContext().put("location", "this is a place.");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(nestedInterpreter).toString())
        .isEqualTo("hello this is a place.");
    }
  }

  @Test
  public void itDoesNotRescursivelyEvaluateMoreExpressions() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "hello {{ place }}");
      nestedInterpreter.getContext().put("place", "there, {{ location }}");
      nestedInterpreter.getContext().put("location", "this is {{ place }}");

      ExpressionNode node = fixture("simplevar");
      assertThat(node.render(nestedInterpreter).toString())
        .isEqualTo("hello there, this is {{ place }}");
    }
  }

  @Test
  public void itRendersStringRange() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      interpreter.getContext().put("theString", "1234567890");

      ExpressionNode node = fixture("string-range");
      assertThat(node.render(interpreter).toString()).isEqualTo("345");
    }
  }

  @Test
  public void itRenderEchoUndefined() {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withFeatureConfig(
        FeatureConfig.newBuilder().add(ECHO_UNDEFINED, FeatureStrategies.ACTIVE).build()
      )
      .build();
    try (
      var a = JinjavaInterpreter
        .closeablePushCurrent(new Jinjava(config).newInterpreter())
        .get()
    ) {
      JinjavaInterpreter jinjavaInterpreter = a.value();
      jinjavaInterpreter.getContext().put("subject", "this");

      String template =
        "{{ subject | capitalize() }} expression {{ testing.template('hello_world') }} " +
        "has a {{ unknown | lower() }} " +
        "token but {{ unknown | default(\"replaced\") }} and empty {{ '' }}";
      Node node = new TreeParser(jinjavaInterpreter, template).buildTree();
      assertThat(jinjavaInterpreter.render(node))
        .isEqualTo(
          "This expression {{ testing.template('hello_world') }} " +
          "has a {{ unknown | lower() }} token but replaced and empty "
        );
    }
  }

  @Test
  public void itFailsOnUnknownTokensVariables() throws Exception {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    try (
      var a = JinjavaInterpreter
        .closeablePushCurrent(new Jinjava(config).newInterpreter())
        .get()
    ) {
      JinjavaInterpreter jinjavaInterpreter = a.value();
      String jinja = "{{ UnknownToken }}";
      Node node = new TreeParser(jinjavaInterpreter, jinja).buildTree();
      assertThatThrownBy(() -> jinjavaInterpreter.render(node))
        .isInstanceOf(UnknownTokenException.class)
        .hasMessage("Unknown token found: UnknownToken");
    }
  }

  @Test
  public void itFailsOnUnknownTokensOfLoops() throws Exception {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    JinjavaInterpreter jinjavaInterpreter = new Jinjava(config).newInterpreter();

    String jinja = "{% for v in values %} {{ v }} {% endfor %}";
    Node node = new TreeParser(jinjavaInterpreter, jinja).buildTree();
    assertThatThrownBy(() -> jinjavaInterpreter.render(node))
      .isInstanceOf(UnknownTokenException.class)
      .hasMessage("Unknown token found: values");
  }

  @Test
  public void itFailsOnUnknownTokensOfIf() throws Exception {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    try (
      var a = JinjavaInterpreter
        .closeablePushCurrent(new Jinjava(config).newInterpreter())
        .get()
    ) {
      JinjavaInterpreter jinjavaInterpreter = a.value();
      String jinja = "{% if bad  %} BAD {% endif %}";
      Node node = new TreeParser(jinjavaInterpreter, jinja).buildTree();
      assertThatThrownBy(() -> jinjavaInterpreter.render(node))
        .isInstanceOf(UnknownTokenException.class)
        .hasMessageContaining("Unknown token found: bad");
    }
  }

  @Test
  public void itFailsOnUnknownTokensWithFilter() throws Exception {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    try (
      var a = JinjavaInterpreter
        .closeablePushCurrent(new Jinjava(config).newInterpreter())
        .get()
    ) {
      JinjavaInterpreter jinjavaInterpreter = a.value();
      String jinja = "{{ UnknownToken }}";
      Node node = new TreeParser(jinjavaInterpreter, jinja).buildTree();
      assertThatThrownBy(() -> jinjavaInterpreter.render(node))
        .isInstanceOf(UnknownTokenException.class)
        .hasMessage("Unknown token found: UnknownToken");
    }
  }

  @Test
  public void valueExprWithOr() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      interpreter.getContext().put("a", "foo");
      interpreter.getContext().put("b", "bar");
      interpreter.getContext().put("c", "");
      interpreter.getContext().put("d", 0);

      assertThat(val("{{ a or b }}")).isEqualTo("foo");
      assertThat(val("{{ c or a }}")).isEqualTo("foo");
      assertThat(val("{{ d or b }}")).isEqualTo("bar");
    }
  }

  @Test
  public void itEscapesValueWhenContextSet() throws Exception {
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      interpreter = a.value();
      interpreter.getContext().put("a", "foo < bar");
      assertThat(val("{{ a }}")).isEqualTo("foo < bar");

      interpreter.getContext().setAutoEscape(true);
      assertThat(val("{{ a }}")).isEqualTo("foo &lt; bar");
    }
  }

  @Test
  public void itIgnoresParseErrorsWhenFeatureIsEnabled() {
    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withFeatureConfig(
        FeatureConfig
          .newBuilder()
          .add(
            BuiltInFeatures.IGNORE_NESTED_INTERPRETATION_PARSE_ERRORS,
            FeatureStrategies.ACTIVE
          )
          .build()
      )
      .build();
    try (var a = JinjavaInterpreter.closeablePushCurrent(interpreter).get()) {
      nestedInterpreter = a.value();
      nestedInterpreter.getContext().put("myvar", "hello {% if");
      nestedInterpreter.getContext().put("place", "world");

      ExpressionNode node = fixture("simplevar");

      assertThat(node.render(nestedInterpreter).toString()).isEqualTo("hello {% if");
      assertThat(nestedInterpreter.getErrors()).isEmpty();
    }
  }

  private String val(String jinja) {
    return parse(jinja).render(interpreter).getValue();
  }

  private ExpressionNode parse(String jinja) {
    return (ExpressionNode) new TreeParser(interpreter, jinja)
      .buildTree()
      .getChildren()
      .getFirst();
  }

  private ExpressionNode fixture(String name) {
    try {
      return parse(
        Resources.toString(
          Resources.getResource(String.format("varblocks/%s.html", name)),
          StandardCharsets.UTF_8
        )
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
