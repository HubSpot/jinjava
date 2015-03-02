package com.hubspot.jinjava.parse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class TagTokenTest {

  @Test
  public void testParseTag() {
    TagToken t = new TagToken("{% foo %}", 1);
    assertThat(t.getTagName()).isEqualTo("foo");
  }
  
  @Test
  public void testParseTagWithHelpers() {
    TagToken t = new TagToken("{% foo bar %}", 1);
    assertThat(t.getTagName()).isEqualTo("foo");
    assertThat(t.getHelpers().trim()).isEqualTo("bar");
  }
  
  @Test
  public void tagNameIsAllJavaIdentifiers() {
    TagToken t = new TagToken("{%rich_text\"top_left\"%}", 1);
    assertThat(t.getTagName()).isEqualTo("rich_text");
    assertThat(t.getHelpers()).isEqualTo("\"top_left\"");
  }
  
}
