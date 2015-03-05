package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.tree.parse.TextToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.tree.parse.TokenScanner;


public class TokenScannerTest {

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
  
  private List<Token> tokens(String fixture) {
    TokenScanner t = fixture(fixture);
    
    List<Token> tokens = Lists.newArrayList();
    Token token;
    while((token = t.getNextToken()) != null) {
      tokens.add(token);
    }
    
    return tokens;
  }
  
  private TokenScanner fixture(String fixture) {
    try {
      TokenScanner t = new TokenScanner(Resources.toString(Resources.getResource(String.format("parse/tokenizer/%s.jinja", fixture)), 
          StandardCharsets.UTF_8));
      return t;
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  
}
