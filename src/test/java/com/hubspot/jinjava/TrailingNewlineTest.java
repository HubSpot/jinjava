package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.junit.Test;

public class TrailingNewlineTest {

  private static final String TEMPLATE_WITH_TRAILING_NEWLINE = "hello\n";
  private static final String TEMPLATE_WITHOUT_TRAILING_NEWLINE = "hello";
  private static final String TEMPLATE_MULTIPLE_TRAILING_NEWLINES = "hello\n\n";

  // ── keepTrailingNewline=true (legacy default: preserve \n) ─────────────────

  @Test
  public void itKeepsTrailingNewlineIsTrue() {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig.newBuilder().withKeepTrailingNewline(true).build()
    );
    assertThat(jinjava.render(TEMPLATE_WITH_TRAILING_NEWLINE, new HashMap<>()))
      .isEqualTo("hello\n");
  }

  @Test
  public void itStripsTrailingNewlineDefault() {
    // Defaults keepTrailingNewline=false (matching Python behaviour)
    Jinjava jinjava = new Jinjava();
    assertThat(jinjava.render(TEMPLATE_WITH_TRAILING_NEWLINE, new HashMap<>()))
      .isEqualTo("hello");
  }

  // ── keepTrailingNewline=false (Python-compatible: strip trailing \n) ────────

  @Test
  public void itStripsTrailingNewlineIsFalse() {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig.newBuilder().withKeepTrailingNewline(false).build()
    );

    assertThat(jinjava.render(TEMPLATE_WITH_TRAILING_NEWLINE, new HashMap<>()))
      .isEqualTo("hello");
  }

  // ── Edge cases ──────────────────────────────────────────────────────────────

  @Test
  public void itDoesNotAffectOutputWithNoTrailingNewline() {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig.newBuilder().withKeepTrailingNewline(true).build()
    );

    assertThat(jinjava.render(TEMPLATE_WITHOUT_TRAILING_NEWLINE, new HashMap<>()))
      .isEqualTo("hello");
  }

  @Test
  public void itStripsOnlyOneTrailingNewlineNotMultiple() {
    // Python only strips a single trailing newline, not all of them.
    Jinjava jinjava = new Jinjava();
    assertThat(jinjava.render(TEMPLATE_MULTIPLE_TRAILING_NEWLINES, new HashMap<>()))
      .isEqualTo("hello\n");
  }

  @Test
  public void itStripsTrailingNewlineFromRenderedExpressions() {
    Jinjava jinjava = new Jinjava();
    assertThat(jinjava.render("{{ greeting }}\n", ImmutableMap.of("greeting", "hello")))
      .isEqualTo("hello");
  }
}
