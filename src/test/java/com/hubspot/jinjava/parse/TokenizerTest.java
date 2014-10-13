package com.hubspot.jinjava.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.collect.Lists;


public class TokenizerTest {

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
    assertThat(tokens.get(tokens.size() - 1)).isInstanceOf(FixedToken.class);
    assertThat(StringUtils.substringBetween(tokens.get(tokens.size() - 1).toString(), "{~", "~}").trim()).isEqualTo("and here's some extra.");
  }
  
  @Test
  public void itProperlyTokenizesMultilineCommentTokens() {
    List<Token> tokens = tokens("multiline-comment");
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(2)).isInstanceOf(FixedToken.class);
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
    assertThat(tokens.get(0).getType()).isEqualTo(ParserConstants.TOKEN_TAG);
    assertThat(tokens.get(0).content).contains("label='Blog Comments'");
  }
  
  @Test
  public void testQuotedTag() {
    List<Token> tokens = tokens("html-with-tag-in-attr");
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getType()).isEqualTo(ParserConstants.TOKEN_FIXED);
    assertThat(tokens.get(1).getType()).isEqualTo(ParserConstants.TOKEN_TAG);
    assertThat(tokens.get(2).getType()).isEqualTo(ParserConstants.TOKEN_FIXED);
  }
  
  @Test
  public void testEscapedQuoteWithinAttrValue() {
    List<Token> tokens = tokens("tag-with-quot-in-attr");
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(ParserConstants.TOKEN_TAG);
    assertThat(tokens.get(0).content.trim()).isEqualTo("widget_block rich_text \"module\" overrideable=True, label='<p>We\\'ve included a great symbol</p>'");
  }
  
  private List<Token> tokens(String fixture) {
    try {
      Tokenizer t = fixture(fixture);
      
      List<Token> tokens = Lists.newArrayList();
      Token token;
      while((token = t.getNextToken()) != null) {
        tokens.add(token);
      }
      
      return tokens;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
  
  private Tokenizer fixture(String fixture) {
    try {
      Tokenizer t = new Tokenizer();
      t.init(Resources.toString(Resources.getResource(String.format("parse/tokenizer/%s.jinja", fixture)), Charsets.UTF_8));
      return t;
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  
}
