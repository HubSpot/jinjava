package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.JinjavaConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
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

  @Test
  public void trimBlocksTracksTheNextTokenPosition() {
    for (String block : List.of("{% set x = 1 %}", "{# comment #}")) {
      for (String newline : List.of("\n", "\r\n")) {
        for (List<Token> tokens : scanBoth(block + newline + "  {{ x }}", true)) {
          assertThat(tokens)
            .extracting(Token::getImage)
            .containsExactly(block, "  ", "{{ x }}");
          assertThat(tokens.get(0).getLineNumber()).isEqualTo(1);
          assertThat(tokens.get(2).getLineNumber()).isEqualTo(2);
          assertThat(tokens.get(2).getStartPosition()).isEqualTo(3);
        }
      }
    }
  }

  @Test
  public void trimBlocksConsumesOnlyOneNewline() {
    for (String block : List.of("{% set x = 1 %}", "{# comment #}")) {
      for (String first : List.of("\n", "\r\n")) {
        for (String second : List.of("\n", "\r\n")) {
          for (List<Token> tokens : scanBoth(block + first + second + "{{ x }}", true)) {
            assertThat(tokens)
              .extracting(Token::getImage)
              .containsExactly(block, second, "{{ x }}");
            assertThat(tokens.get(2).getLineNumber()).isEqualTo(3);
            assertThat(tokens.get(2).getStartPosition()).isEqualTo(1);
          }
        }
      }
    }
  }

  @Test
  public void trimBlocksHandlesNewlinesAtEndOfInput() {
    for (String block : List.of("{% set x = 1 %}", "{# comment #}")) {
      for (String newline : List.of("\n", "\r\n")) {
        for (List<Token> tokens : scanBoth(block + newline, true)) {
          assertThat(tokens).extracting(Token::getImage).containsExactly(block);
        }
      }
    }
  }

  @Test
  public void trimBlocksPreservesStandaloneCarriageReturnsAndInterveningWhitespace() {
    for (String block : List.of("{% set x = 1 %}", "{# comment #}")) {
      for (String suffix : List.of("", "\r", "\rtext", " \r\n", "\t\n", "\r\r\n")) {
        String input = block + suffix;
        for (List<Token> tokens : scanBoth(input, true)) {
          assertThat(tokens.stream().map(Token::getImage).collect(Collectors.joining()))
            .isEqualTo(input);
        }
      }
    }
  }

  @Test
  public void trimBlocksDoesNotTrimAfterExpressions() {
    for (String newline : List.of("\n", "\r\n")) {
      String input = "{{ 1 }}" + newline + "text";
      for (List<Token> tokens : scanBoth(input, true)) {
        assertThat(tokens)
          .extracting(Token::getImage)
          .containsExactly("{{ 1 }}", newline + "text");
      }
    }
  }

  @Test
  public void disabledTrimBlocksPreservesNewlines() {
    for (String block : List.of("{% set x = 1 %}", "{# comment #}")) {
      for (String newline : List.of("\n", "\r\n")) {
        for (List<Token> tokens : scanBoth(block + newline + "text", false)) {
          assertThat(tokens)
            .extracting(Token::getImage)
            .containsExactly(block, newline + "text");
        }
      }
    }
  }

  private List<List<Token>> scanBoth(String input, boolean trimBlocks) {
    JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withTrimBlocks(trimBlocks)
      .build();
    return List.of(
      Lists.newArrayList(new TokenScanner(input, config)),
      Lists.newArrayList(new StringTokenScanner(input, config))
    );
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
    return BaseJinjavaTest.newConfigBuilder().withTrimBlocks(true).build();
  }
}
