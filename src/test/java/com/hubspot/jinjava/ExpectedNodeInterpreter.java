package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExpectedNodeInterpreter {

  private JinjavaInterpreter interpreter;
  private Tag tag;
  private String path;

  public ExpectedNodeInterpreter(JinjavaInterpreter interpreter, Tag tag, String path) {
    this.interpreter = interpreter;
    this.tag = tag;
    this.path = path;
  }

  public String assertExpectedOutput(String name) {
    TagNode tagNode = (TagNode) fixture(name);
    String output = tag.interpret(tagNode, interpreter);
    assertThat(ExpectedTemplateInterpreter.prettify(output.trim()))
      .isEqualTo(ExpectedTemplateInterpreter.prettify(expected(name).trim()));
    return output;
  }

  public Node fixture(String name) {
    try {
      return new TreeParser(
        interpreter,
        ExpectedTemplateInterpreter.simplify(
          Resources.toString(
            Resources.getResource(String.format("%s/%s.jinja", path, name)),
            StandardCharsets.UTF_8
          )
        )
      )
        .buildTree()
        .getChildren()
        .getFirst();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String expected(String name) {
    try {
      return ExpectedTemplateInterpreter.simplify(
        Resources.toString(
          Resources.getResource(String.format("%s/%s.expected.jinja", path, name)),
          StandardCharsets.UTF_8
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
