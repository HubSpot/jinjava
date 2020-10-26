package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class RawTagTest {
  Jinjava jinjava;
  JinjavaInterpreter interpreter;
  RawTag tag;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
    tag = new RawTag();
  }

  @Test
  public void renderPlain() {
    TagNode tagNode = fixture("plain");
    assertThat(tag.interpret(tagNode, interpreter)).isEqualTo("hello world.");
  }

  @Test
  public void renderTags() {
    List<Node> tags = fixtures("tags");
    String result = "";

    for (Node n : tags) {
      TagNode tn = (TagNode) n;
      result += tag.interpret(tn, interpreter);
    }

    assertThat(result).isEqualTo("{{ if list.123 }}foo");
  }

  @Test
  public void renderHublSnippet() {
    TagNode tagNode = fixture("hubl");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .isEqualTo(
        "<h1>Blog Posts</h1> <ul> {% for content in contents %} <li>{{ content.name|title }}</li> {% endfor %} </ul>"
      );
  }

  @Test
  public void itDoesntProcessUnknownTagsWithinARawBlock() {
    TagNode tagNode = fixture("unknowntags");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .isEqualTo("{% footag %}{% bartag %}");
  }

  @Test
  public void itWorksWithInvalidSyntaxWithinRawBlock() {
    TagNode tagNode = fixture("invalidsyntax");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .isEqualTo("this is {invalid and wrong");

    tagNode = fixture("invalidsyntax2");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .isEqualTo("this is {{ invalid and wrong");

    tagNode = fixture("invalidsyntax3");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .isEqualTo("this is }invalid and wrong");

    tagNode = fixture("invalidsyntax4");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .isEqualTo("this is }} invalid and wrong");

    tagNode = fixture("invalidsyntax5");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .isEqualTo("this is {% invalid and wrong");
  }

  @Test
  public void itDoesntProcessJinjaCommentsWithinARawBlock() {
    TagNode tagNode = fixture("comment");
    assertThat(StringUtils.normalizeSpace(tag.interpret(tagNode, interpreter)))
      .contains("{{#each people}}");
  }

  @Test
  public void itPreservesRawTags() {
    TagNode tagNode = fixture("hubl");
    JinjavaInterpreter preserveInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContextCopy(),
      JinjavaConfig.newBuilder().withPreserveForFinalPass(true).build()
    );
    String result = tag.interpret(tagNode, preserveInterpreter);
    try {
      assertThat(result)
        .isEqualTo(
          Resources.toString(
            Resources.getResource("tags/rawtag/hubl.jinja"),
            StandardCharsets.UTF_8
          )
        );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void itPreservesDeferredWhilePreservingRawTags() {
    TagNode tagNode = fixture("deferred");
    JinjavaInterpreter preserveInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContextCopy(),
      JinjavaConfig.newBuilder().withPreserveForFinalPass(true).build()
    );
    preserveInterpreter.getContext().put("deferred", DeferredValue.instance());
    interpreter.getContext().put("deferred", DeferredValue.instance());

    String preservedResult = tag.interpret(tagNode, preserveInterpreter);
    String nonPreservedResult = tag.interpret(tagNode, interpreter);
    try {
      assertThat(preservedResult)
        .isEqualTo(
          Resources
            .toString(
              Resources.getResource("tags/rawtag/deferred.jinja"),
              StandardCharsets.UTF_8
            )
            .trim()
        );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertThat(nonPreservedResult).isEqualTo("{{ deferred }}");

    // Should not get evaluated because it's wrapped in a raw tag.
    String deferredRealValue = "Resolved value.";
    preserveInterpreter.getContext().put("deferred", deferredRealValue);
    interpreter.getContext().put("deferred", deferredRealValue);
    String preservedIdempotent = tag.interpret(
      (TagNode) new TreeParser(preserveInterpreter, preservedResult)
        .buildTree()
        .getChildren()
        .getFirst(),
      preserveInterpreter
    );
    String secondPass = tag.interpret(
      (TagNode) new TreeParser(interpreter, preservedResult)
        .buildTree()
        .getChildren()
        .getFirst(),
      interpreter
    );

    assertThat(preservedIdempotent).isEqualTo(preservedResult);
    assertThat(secondPass).isEqualTo("{{ deferred }}");
  }

  private TagNode fixture(String name) {
    return (TagNode) fixtures(name).getFirst();
  }

  private LinkedList<Node> fixtures(String name) {
    try {
      return new TreeParser(
        interpreter,
        Resources.toString(
          Resources.getResource(String.format("tags/rawtag/%s.jinja", name)),
          StandardCharsets.UTF_8
        )
      )
        .buildTree()
        .getChildren();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
