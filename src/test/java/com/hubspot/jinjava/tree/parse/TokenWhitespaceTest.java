package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.JinjavaConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class TokenWhitespaceTest {

  @Test
  public void trimBlocksTrimsAfterTag() {
    List<Token> tokens = scanTokens(
            trimBlocksConfig()
    );
    assertEquals(tokens.get(2).getImage(), "        yay\n    ");
  }

  private List<Token> scanTokens(JinjavaConfig config) {
    try {
      return Lists.newArrayList(
        new TokenScanner(
          Resources.toString(Resources.getResource("parse/tokenizer/whitespace-tags.jinja"), StandardCharsets.UTF_8),
          config
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JinjavaConfig trimBlocksConfig() {
    return JinjavaConfig.newBuilder().withTrimBlocks(true).build();
  }
}
