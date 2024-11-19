package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;

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
      "parse/tokenizer/whitespace-tags.jinja",
      trimBlocksConfig()
    );
    assertThat(tokens.get(2).getImage()).isEqualTo("        yay\n    ");
  }

  @Test
  public void trimBlocksTrimsAfterCommentTag() {
    List<Token> tokens = scanTokens(
      "parse/tokenizer/whitespace-comment-tags.jinja",
      trimBlocksConfig()
    );
    assertThat(tokens.get(2).getImage()).isEqualTo("        yay\n    ");
    assertThat(tokens.get(4).getImage()).isEqualTo("        whoop\n</div>\n");
  }

  private List<Token> scanTokens(String srcPath, JinjavaConfig config) {
    try {
      return Lists.newArrayList(
        new TokenScanner(
          Resources.toString(Resources.getResource(srcPath), StandardCharsets.UTF_8),
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
