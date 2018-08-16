package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class TreeParserTest {

  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
  }

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
  public void itStripsLeftWhiteSpace() throws Exception {
    String expression = "{% for foo in [1,2,3] %}\n{{ foo }}. \n {%- endfor %}";
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
  public void itPreservesInnerWhiteSpace() throws Exception {
    String expression = "{% for foo in [1,2,3] -%}\nL{% if true %}\n{{ foo }}\n{% endif %}R\n{%- endfor %}";
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
  public void itStripsRightWhiteSpaceAfterTag() throws Exception {
    String expression = ".\n {% for foo in [1,2,3] %} {{ foo }} {% endfor -%} \n.";
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
  public void trimAndLstripBlocks() {
    interpreter = new Jinjava(JinjavaConfig.newBuilder().withLstripBlocks(true).withTrimBlocks(true).build()).newInterpreter();

    assertThat(interpreter.render(parse("parse/tokenizer/whitespace-tags.jinja")))
        .isEqualTo("<div>\n"
            + "        yay\n"
            + "</div>\n");
  }

  Node parse(String fixture) {
    try {
      return new TreeParser(interpreter, Resources.toString(
          Resources.getResource(fixture), StandardCharsets.UTF_8)).buildTree();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

}
