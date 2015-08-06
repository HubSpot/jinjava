package com.hubspot.jinjava.tree.parse;

import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_EXPR_START;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_FIXED;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_NOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class TokenScannerTest {
  JinjavaInterpreter interpreter;
  TokenScanner scanner;
  String script;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
  }

  @Test
  public void test2() {
    script = "{{abc.b}}{% if x %}{{abc{%endif";
    scanner = new TokenScanner(script);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("{{abc{%endif", scanner.next().content.trim());
  }

  @Test
  public void test3() {
    script = "{{abc.b}}{% if x %}{{{abc}}{%endif%}";
    scanner = new TokenScanner(script);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    Token tk = scanner.next();
    assertEquals("{{{abc}}", tk.image);
    assertEquals(TOKEN_EXPR_START, tk.getType());
    assertEquals("{%endif%}", scanner.next().image);
  }

  @Test
  public void test4() {
    script = "{{abc.b}}{% if x %}{{!abc}}{%endif%}";
    scanner = new TokenScanner(script);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    Token tk = scanner.next();
    assertEquals("{{!abc}}", tk.image);
    assertEquals(TOKEN_EXPR_START, tk.getType());
    assertEquals("{%endif%}", scanner.next().image);
  }

  @Test
  public void test5() {
    script = "{{abc.b}}{% if x %}a{{abc}\\}{%endif%}";
    scanner = new TokenScanner(script);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{%endif%}", scanner.next().content.trim());
  }

  @Test
  public void test7() {
    script = "a{{abc.b}}{% if x   %}a{{abc!}#}%}}}{%endif";
    scanner = new TokenScanner(script);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc!}#}%}}", scanner.next().image);
    assertEquals("}", scanner.next().content.trim());
    assertEquals(TOKEN_FIXED, scanner.next().getType());
  }

  @Test
  public void test8() {
    script = "a{{abc.b}}{% if x   %}a{{abc}}{%endif{{";
    scanner = new TokenScanner(script);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals(TOKEN_EXPR_START, scanner.next().getType());
    assertEquals("{%endif{{", scanner.next().content.trim());
  }

  @Test
  public void test9() {
    script = "a{{abc.b}}{% if x   %}a{{abc}\\}{%endif{";
    scanner = new TokenScanner(script);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals(TOKEN_FIXED, scanner.next().getType());
  }

  @Test
  public void test10() {
    script = "a{{abc.b}}{% if x %}a{{abc}\\}{{#%endif{";
    scanner = new TokenScanner(script);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{", scanner.next().image);
    assertEquals(TOKEN_NOTE, scanner.next().getType());
  }

  @Test
  public void test11() {
    script = "a{#abc.b#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script);
    assertEquals("a", scanner.next().image);
    assertEquals("{#abc.b#}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{{{", scanner.next().content.trim());
    assertEquals("{#endif{", scanner.next().image);
  }

  @Test
  public void test12() {
    script = "{#abc.b#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script);
    assertEquals("{#abc.b#}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{{{", scanner.next().content.trim());
    assertEquals(TOKEN_NOTE, scanner.next().getType());
  }

  @Test
  public void test13() {
    script = "{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script);
    assertEquals("{#abc{#.b#}", scanner.next().image);
  }

  @Test
  public void test14() {
    script = "abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script);
    assertEquals("abc", scanner.next().image);
    assertEquals("{#.b#}", scanner.next().image);
    assertEquals("{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}", scanner.next().image);
  }

  @Test
  public void test15() {
    script = "abc{#.b#}{#xy{!ad!}{#DD#}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script);
    assertEquals("abc", scanner.next().image);
    assertEquals("{#.b#}", scanner.next().image);
    assertEquals("{#xy{!ad!}{#DD#}", scanner.next().image);
  }

  @Test
  public void test16() {
    script = "{#{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script);
    assertEquals("{#{#abc{#.b#}", scanner.next().image);
  }

  @Test
  public void test17() {
    script = "{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}#}a#}{{abc}\\}#}{{{{#endif{";
    scanner = new TokenScanner(script);
    assertEquals("{#abc{#.b#}", scanner.next().image);
  }

  @Test
  public void itProperlyTokenizesCommentBlocksContainingTags() {
    List<Token> tokens = tokens("comment-with-tags");
    assertThat(tokens).hasSize(5);
    assertThat(tokens.get(4)).isInstanceOf(TagToken.class);
    assertThat(StringUtils.substringBetween(tokens.get(4).toString(), "{%", "%}").trim()).isEqualTo("endif");
  }

  @Test
  public void itProperlyTokenizesCommentWithTrailingTokens() {
    List<Token> tokens = tokens("comment-plus");
    assertThat(tokens).hasSize(2);
    assertThat(tokens.get(tokens.size() - 1)).isInstanceOf(TextToken.class);
    assertThat(StringUtils.substringBetween(tokens.get(tokens.size() - 1).toString(), "{~", "~}").trim()).isEqualTo("and here's some extra.");
  }

  @Test
  public void itProperlyTokenizesMultilineCommentTokens() {
    List<Token> tokens = tokens("multiline-comment");
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(2)).isInstanceOf(TextToken.class);
    assertThat(StringUtils.substringBetween(tokens.get(2).toString(), "{~", "~}").trim()).isEqualTo("goodbye.");
  }

  @Test
  public void itProperlyTokenizesCommentWithStartCommentTokenWithin() {
    List<Token> tokens = tokens("comment-with-start-comment-within");
    assertThat(tokens).hasSize(2);
    assertThat(tokens.get(1)).isInstanceOf(TagToken.class);
    assertThat(tokens.get(1).getImage()).contains("color");
  }

  @Test
  public void itProperlyTokenizesTagTokenWithTagTokenCharsWithinString() {
    List<Token> tokens = tokens("tag-with-tag-tokens-within-string");
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(TokenScannerSymbols.TOKEN_TAG);
    assertThat(tokens.get(0).content).contains("label='Blog Comments'");
  }

  @Test
  public void testQuotedTag() {
    List<Token> tokens = tokens("html-with-tag-in-attr");
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getType()).isEqualTo(TokenScannerSymbols.TOKEN_FIXED);
    assertThat(tokens.get(1).getType()).isEqualTo(TokenScannerSymbols.TOKEN_TAG);
    assertThat(tokens.get(2).getType()).isEqualTo(TokenScannerSymbols.TOKEN_FIXED);
  }

  @Test
  public void testEscapedQuoteWithinAttrValue() {
    List<Token> tokens = tokens("tag-with-quot-in-attr");
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(TokenScannerSymbols.TOKEN_TAG);
    assertThat(tokens.get(0).content.trim()).isEqualTo("widget_block rich_text \"module\" overrideable=True, label='<p>We\\'ve included a great symbol</p>'");
  }

  @Test
  public void testEscapedBackslashWithinAttrValue() {
    List<Token> tokens = tokens("escape-char-tokens");

    List<String> tagHelpers = tokens.stream()
        .filter(t -> t.getType() == TokenScannerSymbols.TOKEN_TAG)
        .map(t -> ((TagToken) t).getHelpers().trim().substring(1, 26))
        .collect(Collectors.toList());

    assertThat(tagHelpers).containsExactly(
        "module_143819779285827357",
        "module_143819780991527688",
        "module_143819781983527999");
  }

  private List<Token> tokens(String fixture) {
    TokenScanner t = fixture(fixture);

    List<Token> tokens = Lists.newArrayList();
    Token token;
    while ((token = t.getNextToken()) != null) {
      tokens.add(token);
    }

    return tokens;
  }

  private TokenScanner fixture(String fixture) {
    try {
      TokenScanner t = new TokenScanner(Resources.toString(Resources.getResource(String.format("parse/tokenizer/%s.jinja", fixture)),
          StandardCharsets.UTF_8));
      return t;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
