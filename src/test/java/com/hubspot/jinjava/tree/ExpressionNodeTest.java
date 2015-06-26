package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.AutoEscapeTag;

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
    assertThat(node.render(interpreter)).isEqualTo("hello world");
  }

  @Test
  public void itAvoidsInfiniteRecursionWhenVarsContainBraceBlocks() throws Exception {
    context.put("myvar", "hello {{ place }}");
    context.put("place", "{{ place }}");

    ExpressionNode node = fixture("simplevar");
    assertThat(node.render(interpreter)).isEqualTo("hello {{ place }}");
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

    context.put(AutoEscapeTag.AUTOESCAPE_CONTEXT_VAR, Boolean.TRUE);
    assertThat(val("{{ a }}")).isEqualTo("foo &lt; bar");
  }

  private String val(String jinja) {
    return parse(jinja).render(interpreter);
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
