package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.lib.tag.ForTagTest;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class EagerForTagTest extends ForTagTest {

  @Before
  public void setup() {
    super.setup();
    this.context.registerTag(new EagerForTag());
    tag = new EagerForTag();
    this.context.put("deferred", DeferredValue.instance());
  }

  @Test
  public void itRegistersEagerToken() {
    assertExpectedOutput("registers-eager-token");
    Optional<EagerTagToken> maybeEagerTagToken = context
      .getEagerTagTokens()
      .stream()
      .filter(e -> e.getTagToken().getTagName().equals(tag.getName()))
      .findAny();
    assertThat(maybeEagerTagToken).isPresent();
    assertThat(maybeEagerTagToken.get().getDeferredHelpers()).containsExactly("item");
  }

  private String assertExpectedOutput(String name) {
    TagNode tagNode = (TagNode) fixture(name);
    String output = tag.interpret(tagNode, interpreter);
    assertThat(output.trim()).isEqualTo(expected(name));
    return output;
  }

  private Node fixture(String name) {
    try {
      return new TreeParser(
        interpreter,
        Resources.toString(
          Resources.getResource(String.format("tags/eager/fortag/%s.jinja", name)),
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

  private String expected(String name) {
    try {
      return Resources.toString(
        Resources.getResource(String.format("tags/eager/fortag/%s.expected.jinja", name)),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
