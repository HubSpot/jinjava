package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import org.junit.Test;

import com.hubspot.jinjava.interpret.TemplateSyntaxException;

public class TagTokenTest {

  @Test
  public void testParseTag() {
    TagToken t = new TagToken("{% foo %}", 1, 2);
    assertThat(t.getTagName()).isEqualTo("foo");
  }

  @Test
  public void testParseTagWithHelpers() {
    TagToken t = new TagToken("{% foo bar %}", 1, 2);
    assertThat(t.getTagName()).isEqualTo("foo");
    assertThat(t.getHelpers().trim()).isEqualTo("bar");
  }

  @Test
  public void tagNameIsAllJavaIdentifiers() {
    TagToken t = new TagToken("{%rich_text\"top_left\"%}", 1, 2);
    assertThat(t.getTagName()).isEqualTo("rich_text");
    assertThat(t.getHelpers()).isEqualTo("\"top_left\"");
  }

  @Test
  public void itThrowsParseErrorWhenMalformed() {
    try {
      new TagToken("{% ", 1, 2);
      failBecauseExceptionWasNotThrown(TemplateSyntaxException.class);
    } catch (TemplateSyntaxException e) {
      assertThat(e).hasMessageContaining("Malformed");
    }
  }

}
