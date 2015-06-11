package com.hubspot.jinjava.tree;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
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
    assertThat(interpreter.getErrors()).isEmpty();
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
