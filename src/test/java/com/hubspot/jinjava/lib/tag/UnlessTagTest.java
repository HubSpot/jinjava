package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;

public class UnlessTagTest extends BaseInterpretingTest {

  public Tag tag;

  @Before
  public void setupTag() {
    tag = new UnlessTag();
  }

  @Test
  public void itEvaluatesChildrenWhenExpressionIsFalse() throws Exception {
    context.put("foo", "bar");
    TagNode n = fixture("unless");
    assertThat(tag.interpret(n, interpreter).trim()).isEqualTo("");
  }

  @Test
  public void itDoesntEvalChildrenWhenExprIsTrue() throws Exception {
    context.put("foo", null);
    TagNode n = fixture("unless");
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
