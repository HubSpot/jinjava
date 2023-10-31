package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class TreeParserTest extends BaseInterpretingTest {

  @Test
  public void parseHtmlWithCommentLines() {
    parse("parse/tokenizer/comment-plus.jinja");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itStripsRightWhiteSpace() throws Exception {
    String expression = "{% for foo in [1,2,3] -%} \n .{{ foo }}\n{% endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".1\n.2\n.3\n");
  }

  @Test
  public void itStripsRightWhiteSpaceWithComment() throws Exception {
    String expression =
      "{% for foo in [1,2,3] -%} \n {#- comment -#} \n {#- comment -#} .{{ foo }}\n{% endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".1\n.2\n.3\n");
  }

  @Test
  public void itStripsLeftWhiteSpace() throws Exception {
    String expression = "{% for foo in [1,2,3] %}\n{{ foo }}. \n {%- endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("\n1.\n2.\n3.");
  }

  @Test
  public void itStripsLeftWhiteSpaceWithComment() throws Exception {
    String expression =
      "{% for foo in [1,2,3] %}\n{{ foo }}. \n {#- comment -#} {%- endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("\n1.\n2.\n3.");
  }

  @Test
  public void itStripsLeftAndRightWhiteSpace() throws Exception {
    String expression = "{% for foo in [1,2,3] -%} \n .{{ foo }}. \n {%- endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".1..2..3.");
  }

  @Test
  public void itStripsLeftAndRightWhiteSpaceWithComment() throws Exception {
    String expression =
      "{% for foo in [1,2,3] -%} \n {#- comment -#} \n {#- comment -#} .{{ foo }}. \n {#- comment -#} \n {#- comment -#} {%- endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".1..2..3.");
  }

  @Test
  public void itPreservesInnerWhiteSpace() throws Exception {
    String expression =
      "{% for foo in [1,2,3] -%}\nL{% if true %}\n{{ foo }}\n{% endif %}R\n{%- endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("L\n1\nRL\n2\nRL\n3\nR");
  }

  @Test
  public void itPreservesInnerWhiteSpaceWithComment() throws Exception {
    String expression =
      "{% for foo in [1,2,3] -%}\n {#- comment -#} \n {#- comment -#}L{% if true %}\n{{ foo }}\n{% endif %}R\n {#- comment -#} \n {#- comment -#}{%- endfor %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("L\n1\nRL\n2\nRL\n3\nR");
  }

  @Test
  public void itStripsLeftWhiteSpaceBeforeTag() throws Exception {
    String expression = ".\n {%- for foo in [1,2,3] %} {{ foo }} {% endfor %} \n.";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(". 1  2  3  \n.");
  }

  @Test
  public void itStripsLeftWhiteSpaceBeforeTagWithComment() throws Exception {
    String expression =
      ".\n {#- comment -#} \n {#- comment -#} {%- for foo in [1,2,3] %} {{ foo }} {% endfor %} \n.";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(". 1  2  3  \n.");
  }

  @Test
  public void itStripsRightWhiteSpaceAfterTag() throws Exception {
    String expression = ".\n {% for foo in [1,2,3] %} {{ foo }} {% endfor -%} \n.";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".\n  1  2  3 .");
  }

  @Test
  public void itStripsRightWhiteSpaceAfterTagWithComment() throws Exception {
    String expression =
      ".\n {% for foo in [1,2,3] %} {{ foo }} {% endfor -%} \n {#- comment -#} \n {#- comment -#}.";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".\n  1  2  3 .");
  }

  @Test
  public void itStripsAllOuterWhiteSpace() throws Exception {
    String expression = ".\n {%- for foo in [1,2,3] -%} {{ foo }} {%- endfor -%} \n.";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".123.");
  }

  @Test
  public void itStripsAllOuterWhiteSpaceForInlineTags() throws Exception {
    String expression = "1\n\n{%- print 2 -%}\n\n3\n\n{%- set x = 1 -%}\n\n4";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("1234");
  }

  @Test
  public void itStripsAllOuterWhiteSpaceWithComment() throws Exception {
    String expression =
      ".\n {#- comment -#} \n {#- comment -#} {%- for foo in [1,2,3] -%} {{ foo }} {%- endfor -%} \n {#- comment -#} \n {#- comment -#}.";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo(".123.");
  }

  @Test
  public void trimAndLstripBlocks() {
    interpreter =
      new Jinjava(
        JinjavaConfig.newBuilder().withLstripBlocks(true).withTrimBlocks(true).build()
      )
      .newInterpreter();

    assertThat(interpreter.render(parse("parse/tokenizer/whitespace-tags.jinja")))
      .isEqualTo("<div>\n" + "        yay\n" + "</div>\n");
  }

  @Test
  public void itWarnsAgainstMissingStartTags() {
    String expression = "{% if true %} foo {% endif %} {% endif %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.getErrors()).hasSize(1);
    assertThat(interpreter.getErrors().get(0).getSeverity()).isEqualTo(ErrorType.WARNING);
    assertThat(interpreter.getErrors().get(0).getMessage())
      .isEqualTo("Missing start tag");
    assertThat(interpreter.getErrors().get(0).getFieldName()).isEqualTo("endif");
  }

  @Test
  public void itWarnsAgainstUnclosedComment() {
    String expression = "foo {# this is an unclosed comment %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.getErrors()).hasSize(1);
    assertThat(interpreter.getErrors().get(0).getSeverity()).isEqualTo(ErrorType.WARNING);
    assertThat(interpreter.getErrors().get(0).getMessage()).isEqualTo("Unclosed comment");
    assertThat(interpreter.getErrors().get(0).getFieldName()).isEqualTo("comment");
  }

  @Test
  public void itWarnsAgainstUnclosedExpression() {
    String expression = "foo {{ this is an unclosed expression %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.getErrors()).hasSize(1);
    assertThat(interpreter.getErrors().get(0).getSeverity()).isEqualTo(ErrorType.WARNING);
    assertThat(interpreter.getErrors().get(0).getMessage()).isEqualTo("Unclosed token");
    assertThat(interpreter.getErrors().get(0).getFieldName()).isEqualTo("token");
    assertThat(interpreter.render(tree))
      .isEqualTo("foo {{ this is an unclosed expression %}");
  }

  @Test
  public void itWarnsAgainstUnclosedTag() {
    String expression = "foo {% this is an unclosed tag }}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.getErrors()).hasSize(1);
    assertThat(interpreter.getErrors().get(0).getSeverity()).isEqualTo(ErrorType.WARNING);
    assertThat(interpreter.getErrors().get(0).getMessage()).isEqualTo("Unclosed token");
    assertThat(interpreter.getErrors().get(0).getFieldName()).isEqualTo("token");
    assertThat(interpreter.render(tree)).isEqualTo("foo {% this is an unclosed tag }}");
  }

  @Test
  public void itWarnsTwiceAgainstUnclosedForTag() {
    String expression = "{% for item in list %}\n{% for elem in items %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.getErrors()).hasSize(2);
    assertThat(interpreter.getErrors().get(0).getFieldName())
      .isEqualTo("{% for elem in items %}");
    assertThat(interpreter.getErrors().get(0).getLineno()).isEqualTo(2);
    assertThat(interpreter.getErrors().get(1).getFieldName())
      .isEqualTo("{% for item in list %}");
    assertThat(interpreter.getErrors().get(1).getLineno()).isEqualTo(1);
  }

  @Test
  public void itWarnsTwiceAgainstUnclosedIfTag() {
    String expression = "{% if 1 > 2 %}\n{% if 2 > 1 %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.getErrors()).hasSize(2);
    assertThat(interpreter.getErrors().get(0).getFieldName()).isEqualTo("{% if 2 > 1 %}");
    assertThat(interpreter.getErrors().get(0).getLineno()).isEqualTo(2);
    assertThat(interpreter.getErrors().get(1).getFieldName()).isEqualTo("{% if 1 > 2 %}");
    assertThat(interpreter.getErrors().get(1).getLineno()).isEqualTo(1);
  }

  @Test
  public void itWarnsTwiceAgainstUnclosedBlockTag() {
    String expression = "{% block first %}\n{% block second %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.getErrors()).hasSize(2);
    assertThat(interpreter.getErrors().get(0).getFieldName())
      .isEqualTo("{% block second %}");
    assertThat(interpreter.getErrors().get(0).getLineno()).isEqualTo(2);
    assertThat(interpreter.getErrors().get(1).getFieldName())
      .isEqualTo("{% block first %}");
    assertThat(interpreter.getErrors().get(1).getLineno()).isEqualTo(1);
  }

  @Test
  public void itTrimsNotes() {
    String expression = "A\n{#- note -#}\nB";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("AB");
  }

  @Test
  public void itMergesTextNodesWhileRespectingTrim() {
    String expression = "{% print 'A' -%}\n{#- note -#}\nB\n{%- print 'C' %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("ABC");
  }

  @Test
  public void itTrimsExpressions() {
    String expression = "A\n{{- 'B' -}}\nC";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("ABC");
  }

  @Test
  public void itDoesNotMergeAdjacentTextNodesWhenLegacyOverrideIsApplied() {
    String expression = "A\n{%- if true -%}\n{# comment #}\nB{% endif %}";
    final Node tree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(tree)).isEqualTo("AB");
    interpreter =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withAllowAdjacentTextNodes(true).build()
          )
          .build()
      )
      .newInterpreter();
    final Node overriddenTree = new TreeParser(interpreter, expression).buildTree();
    assertThat(interpreter.render(overriddenTree)).isEqualTo("A\nB");
  }

  Node parse(String fixture) {
    try {
      return new TreeParser(
        interpreter,
        Resources.toString(Resources.getResource(fixture), StandardCharsets.UTF_8)
      )
      .buildTree();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
