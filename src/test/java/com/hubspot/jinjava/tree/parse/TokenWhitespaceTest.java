package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.JinjavaConfig;

public class TokenWhitespaceTest {

  @Test
  public void trimBlocksTrimsAfterTag() {
    List<Token> tokens = scanTokens("parse/tokenizer/whitespace-tags.jinja", trimBlocksConfig());
    assertThat(tokens.get(2).getImage()).isEqualTo("        yay\n    ");
  }

  private List<Token> scanTokens(String srcPath, JinjavaConfig config) {
    try {
      return Lists.newArrayList(new TokenScanner(
          Resources.toString(Resources.getResource(srcPath), StandardCharsets.UTF_8), config));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private JinjavaConfig trimBlocksConfig() {
    return JinjavaConfig.newBuilder().withTrimBlocks(true).build();
  }

}
