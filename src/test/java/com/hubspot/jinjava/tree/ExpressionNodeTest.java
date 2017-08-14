package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ExpressionNodeTest {

  private Context context;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    context = interpreter.getContext();
  }

  @Test
  public void itRendersResultAsTemplateWhenContainingVarBlocks() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "world");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("hello world");
  }

  @Test
  public void itRendersResultWithoutNestedExpressionInterpretation() throws Exception {
    final JinjavaConfig config = JinjavaConfig.newBuilder().withNestedInterpretationEnabled(false).build();
    JinjavaInterpreter noNestedInterpreter =  new Jinjava(config).newInterpreter();
    Context contextNoNestedInterpretation = noNestedInterpreter.getContext();
    contextNoNestedInterpretation.put("myvar", "hello {{ place }}");
    contextNoNestedInterpretation.put("place", "world");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(noNestedInterpreter).toString()).isEqualTo("hello {{ place }}");
  }

  @Test
  public void itRendersWithNestedExpressionInterpretationByDefault() throws Exception {
    final JinjavaConfig config = JinjavaConfig.newBuilder().build();
    JinjavaInterpreter noNestedInterpreter =  new Jinjava(config).newInterpreter();
    Context contextNoNestedInterpretation = noNestedInterpreter.getContext();
    contextNoNestedInterpretation.put("myvar", "hello {{ place }}");
    contextNoNestedInterpretation.put("place", "world");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(noNestedInterpreter).toString()).isEqualTo("hello world");
  }

  @Test
  public void itAvoidsInfiniteRecursionWhenVarsContainBraceBlocks() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "{{ place }}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("hello {{ place }}");
  }

  @Test
  public void itAvoidsInfiniteRecursionOfItself() throws Exception {
    context.put("myvar", "hello {{myvar}}");

    ExpressionNode node = fixture("simplevar");
    // It renders once, and then stop further rendering after detecting recursion.
    assertThat(node.render(interpreter).toString()).isEqualTo("hello hello {{myvar}}");
  }

  @Test
  public void itNoRecursionHere() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "{{location}}");
    context.put("location", "this is a place.");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("hello this is a place.");
  }

  @Test
  public void itAvoidsInfiniteRecursion() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "there, {{ location }}");
    context.put("location", "this is {{ place }}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter).toString()).isEqualTo("hello there, this is {{ place }}");
  }

  @Test
  public void itRendersStringRange() throws Exception {
    context.put("theString", "1234567890");

    ExpressionNode node = fixture("string-range");
    assertThat(node.render(interpreter).toString()).isEqualTo("345");
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

  private String val(String jinja) {
    return parse(jinja).render(interpreter).getValue();
  }

  private ExpressionNode parse(String jinja) {
    return (ExpressionNode) new TreeParser(interpreter, jinja).buildTree().getChildren().getFirst();
  }

  private ExpressionNode fixture(String name) {
    try {
      return parse(Resources.toString(Resources.getResource(String.format("varblocks/%s.html", name)), StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

}
