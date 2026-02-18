package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.testobjects.IfTagTestObject;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;

public class IfTagTest extends BaseInterpretingTest {

  public Tag tag;

  @Before
  public void setup() {
    tag = new IfTag();
  }

  @Test
  public void itEvaluatesChildrenWhenExpressionIsTrue() {
    context.put("foo", "bar");
    TagNode n = fixture("if");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("ifblock");
  }

  @Test
  public void itDoesntEvalChildrenWhenExprIsFalse() {
    context.put("foo", null);
    TagNode n = fixture("if");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("");
  }

  @Test
  public void itChecksObjectTruthValue() {
    context.put("foo", new IfTagTestObject().setObjectTruthValue(true));
    TagNode n = fixture("if-object");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("ifblock");

    context.put("foo", new IfTagTestObject().setObjectTruthValue(false));
    n = fixture("if-object");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("");
  }

  @Test
  public void itTreatsNotFoundPropsAsNull() {
    context.put("settings", new Object());
    TagNode n = fixture("if-non-existent-prop");
    assertThat(tag.interpret(n, interpreter).trim()).isEmpty();
  }

  @Test
  public void itEvalsIfNotElseWhenExpIsTrue() {
    context.put("foo", "bar");
    TagNode n = fixture("if-else");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("ifblock");
  }

  @Test
  public void itEvalsElseNotIfWhenExpIsFalse() {
    context.put("foo", "");
    TagNode n = fixture("if-else");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("elseblock");
  }

  @Test
  public void itEvalsOnlyIfInElifTreeWhenExpIsTrue() {
    context.put("foo", "bar");
    TagNode n = fixture("if-elif-else");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("ifblock");
  }

  @Test
  public void itEvalsOnlyIfInElifTreeWhenBothAreTrue() {
    context.put("foo", "bar");
    TagNode n = fixture("if-true-elif-true");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("one");
  }

  @Test
  public void itEvalsOnlyElifInTreeWhenExp2IsTrue() {
    context.put("foo", "");
    context.put("bar", "val");
    TagNode n = fixture("if-elif-else");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("elifblock");
  }

  @Test
  public void itEvalsOnlyElseInTreeWhenExpsAllFalse() {
    context.put("foo", "");
    context.put("bar", "");
    TagNode n = fixture("if-elif-else");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("elseblock");
  }

  @Test
  public void itEvalsSecondElifInTreeWhenExp3IsTrue() {
    context.put("foo", "");
    context.put("bar", "");
    context.put("elf", "true");
    TagNode n = fixture("if-elif-elif-else");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("elif2block");
  }

  @Test
  public void itEvalsExprWithFilter() {
    context.put("items", Lists.newArrayList("foo", "bar"));
    TagNode n = fixture("if-expr-filter");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("ifblock");
  }

  private TagNode fixture(String name) {
    try {
      return (TagNode) new TreeParser(
        interpreter,
        Resources.toString(
          Resources.getResource(String.format("tags/iftag/%s.jinja", name)),
          StandardCharsets.UTF_8
        )
      )
        .buildTree()
        .getChildren()
        .getFirst();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
