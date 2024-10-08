package com.hubspot.jinjava.tree;

import static com.hubspot.jinjava.lib.expression.DefaultExpressionStrategy.ECHO_UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class ExpressionNodeTest extends BaseInterpretingTest {

  @Test
  public void itRendersResultAsTemplateWhenContainingVarBlocks() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "world");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("hello world");
  }

  @Test
  public void itRendersResultWithoutNestedExpressionInterpretation() throws Exception {
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withNestedInterpretationEnabled(false)
      .build();
    JinjavaInterpreter noNestedInterpreter = new Jinjava(config).newInterpreter();
    Context contextNoNestedInterpretation = noNestedInterpreter.getContext();
    contextNoNestedInterpretation.put("myvar", "hello {{ place }}");
    contextNoNestedInterpretation.put("place", "world");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(noNestedInterpreter).toString())
      .isEqualTo("hello {{ place }}");
  }

  @Test
  public void itRendersWithNestedExpressionInterpretationByDefault() throws Exception {
    final JinjavaConfig config = JinjavaConfig.newBuilder().build();
    JinjavaInterpreter noNestedInterpreter = new Jinjava(config).newInterpreter();
    Context contextNoNestedInterpretation = noNestedInterpreter.getContext();
    contextNoNestedInterpretation.put("myvar", "hello {{ place }}");
    contextNoNestedInterpretation.put("place", "world");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(noNestedInterpreter).toString()).isEqualTo("hello world");
  }

  @Test
  public void itRendersNestedTags() throws Exception {
    final JinjavaConfig config = JinjavaConfig.newBuilder().build();
    JinjavaInterpreter jinjava = new Jinjava(config).newInterpreter();
    Context context = jinjava.getContext();
    context.put("myvar", "hello {% if (true) %}nasty{% endif %}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(jinjava).toString()).isEqualTo("hello nasty");
  }

  @Test
  public void itAvoidsInfiniteRecursionWhenVarsContainBraceBlocks() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "{{ place }}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("hello {{ place }}");
  }

  @Test
  public void itAllowsNestedTagExpressions() throws Exception {
    context.put("myvar", "{% if true %}{{ place }}{% endif %}");
    context.put("place", "{% if true %}Hello{% endif %}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("Hello");
  }

  @Test
  public void itAvoidsInfiniteRecursionWhenVarsAreInIfBlocks() throws Exception {
    context.put("myvar", "{% if true %}{{ place }}{% endif %}");
    context.put("place", "{% if true %}{{ myvar }}{% endif %}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString())
      .isEqualTo("{% if true %}{{ myvar }}{% endif %}");
  }

  @Test
  public void itDoesNotRescursivelyEvaluateExpressionsOfSelf() throws Exception {
    context.put("myvar", "hello {{myvar}}");

    ExpressionNode node = fixture("simplevar");
    // It renders once, and then stop further rendering after detecting recursion.
    assertThat(node.render(interpreter).toString()).isEqualTo("hello hello {{myvar}}");
  }

  @Test
  public void itDoesNotRescursivelyEvaluateExpressions() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "{{location}}");
    context.put("location", "this is a place.");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("hello this is a place.");
  }

  @Test
  public void itDoesNotRescursivelyEvaluateMoreExpressions() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "there, {{ location }}");
    context.put("location", "this is {{ place }}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString())
      .isEqualTo("hello there, this is {{ place }}");
  }

  @Test
  public void itRendersStringRange() throws Exception {
    context.put("theString", "1234567890");

    ExpressionNode node = fixture("string-range");
    assertThat(node.render(interpreter).toString()).isEqualTo("345");
  }

  @Test
  public void itRenderEchoUndefined() {
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withFeatureConfig(
        FeatureConfig.newBuilder().add(ECHO_UNDEFINED, FeatureStrategies.ACTIVE).build()
      )
      .build();
    final JinjavaInterpreter jinjavaInterpreter = new Jinjava(config).newInterpreter();
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

  @Test
  public void itFailsOnUnknownTokensVariables() throws Exception {
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    JinjavaInterpreter jinjavaInterpreter = new Jinjava(config).newInterpreter();

    String jinja = "{{ UnknownToken }}";
    Node node = new TreeParser(jinjavaInterpreter, jinja).buildTree();
    assertThatThrownBy(() -> jinjavaInterpreter.render(node))
      .isInstanceOf(UnknownTokenException.class)
      .hasMessage("Unknown token found: UnknownToken");
  }

  @Test
  public void itFailsOnUnknownTokensOfLoops() throws Exception {
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
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
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    JinjavaInterpreter jinjavaInterpreter = new Jinjava(config).newInterpreter();

    String jinja = "{% if bad  %} BAD {% endif %}";
    Node node = new TreeParser(jinjavaInterpreter, jinja).buildTree();
    assertThatThrownBy(() -> jinjavaInterpreter.render(node))
      .isInstanceOf(UnknownTokenException.class)
      .hasMessageContaining("Unknown token found: bad");
  }

  @Test
  public void itFailsOnUnknownTokensWithFilter() throws Exception {
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withFailOnUnknownTokens(true)
      .build();
    JinjavaInterpreter jinjavaInterpreter = new Jinjava(config).newInterpreter();

    String jinja = "{{ UnknownToken }}";
    Node node = new TreeParser(jinjavaInterpreter, jinja).buildTree();
    assertThatThrownBy(() -> jinjavaInterpreter.render(node))
      .isInstanceOf(UnknownTokenException.class)
      .hasMessage("Unknown token found: UnknownToken");
  }

  @Test
  public void valueExprWithOr() throws Exception {
    context.put("a", "foo");
    context.put("b", "bar");
    context.put("c", "");
    context.put("d", 0);

    assertThat(val("{{ a or b }}")).isEqualTo("foo");
    assertThat(val("{{ c or a }}")).isEqualTo("foo");
    assertThat(val("{{ d or b }}")).isEqualTo("bar");
  }

  @Test
  public void itEscapesValueWhenContextSet() throws Exception {
    context.put("a", "foo < bar");
    assertThat(val("{{ a }}")).isEqualTo("foo < bar");

    context.setAutoEscape(true);
    assertThat(val("{{ a }}")).isEqualTo("foo &lt; bar");
  }

  @Test
  public void itIgnoresParseErrorsWhenFeatureIsEnabled() {
    final JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withFeatureConfig(
        FeatureConfig
          .newBuilder()
          .add(JinjavaInterpreter.IGNORE_NESTED_INTERPRETATION_PARSE_ERRORS, c -> true)
          .build()
      )
      .build();
    JinjavaInterpreter interpreter = new Jinjava(config).newInterpreter();
    Context context = interpreter.getContext();
    context.put("myvar", "hello {% if");
    context.put("place", "world");

    ExpressionNode node = fixture("simplevar");

    assertThat(node.render(interpreter).toString()).isEqualTo("hello {% if");
    assertThat(interpreter.getErrors()).isEmpty();
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
