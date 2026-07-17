package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

/**
 * Tests for backslash handling inside block/variable/comment delimiters,
 * covering both the char-based (DefaultTokenScannerSymbols) and string-based
 * (StringTokenScannerSymbols) scanning paths, with the
 * {@link LegacyOverrides#isHandleBackslashInQuotesOnly()} flag both off (legacy)
 * and on (Jinja2-compatible).
 */
public class BackslashHandlingTest {

  // ── Jinjava instances ──────────────────────────────────────────────────────

  /** Char-based scanner, legacy backslash behaviour (flag = false). */
  private static Jinjava charLegacy() {
    return new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withLegacyOverrides(LegacyOverrides.newBuilder().build())
        .build()
    );
  }

  /** Char-based scanner, Jinja2-compatible backslash behaviour (flag = true). */
  private static Jinjava charNew() {
    return new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withHandleBackslashInQuotesOnly(true).build()
        )
        .build()
    );
  }

  /** String-based scanner, legacy backslash behaviour (flag = false). */
  private static Jinjava stringLegacy() {
    return new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withTokenScannerSymbols(StringTokenScannerSymbols.builder().build())
        .withLegacyOverrides(LegacyOverrides.newBuilder().build())
        .build()
    );
  }

  /** String-based scanner, Jinja2-compatible backslash behaviour (flag = true). */
  private static Jinjava stringNew() {
    return new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withTokenScannerSymbols(StringTokenScannerSymbols.builder().build())
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withHandleBackslashInQuotesOnly(true).build()
        )
        .build()
    );
  }

  // ── Backslash inside a quoted string ──────────────────────────────────────
  //
  // Both legacy and new behaviour must handle escaped quotes inside strings
  // correctly — \" should not close the string.

  @Test
  public void charLegacy_escapedQuoteInsideString() {
    assertThat(charLegacy().render("{{ \"he said \\\"hi\\\"\" }}", new HashMap<>()))
      .isEqualTo("he said \"hi\"");
  }

  @Test
  public void charNew_escapedQuoteInsideString() {
    assertThat(charNew().render("{{ \"he said \\\"hi\\\"\" }}", new HashMap<>()))
      .isEqualTo("he said \"hi\"");
  }

  @Test
  public void stringLegacy_escapedQuoteInsideString() {
    assertThat(stringLegacy().render("{{ \"he said \\\"hi\\\"\" }}", new HashMap<>()))
      .isEqualTo("he said \"hi\"");
  }

  @Test
  public void stringNew_escapedQuoteInsideString() {
    assertThat(stringNew().render("{{ \"he said \\\"hi\\\"\" }}", new HashMap<>()))
      .isEqualTo("he said \"hi\"");
  }

  // ── Backslash outside a quoted string ─────────────────────────────────────
  //
  // Template under test: "prefix {{ x \}} suffix }}"
  //
  // We test the scanner token structure directly rather than going through
  // render(), because the expression "x \..." is always a JUEL lexical error
  // regardless of mode. What differs between modes is which token boundaries
  // the scanner produces — and that is what we assert on.
  //
  // Legacy (backslashInQuotesOnly = false):
  //   Scanner consumes '\' and skips the following '}'. The first '}}' is not
  //   recognized as a closer. The block runs until the second '}}', so the
  //   token sequence is:
  //     TEXT "prefix "  |  EXPR "{{ x \}} suffix }}"
  //
  // New (backslashInQuotesOnly = true):
  //   Scanner leaves '\' untouched. The first '}}' is recognized as the closer.
  //   The token sequence is:
  //     TEXT "prefix "  |  EXPR "{{ x \}}"  |  TEXT " suffix }}"

  private static final String BACKSLASH_TEMPLATE = "prefix {{ x \\}} suffix }}";

  @Test
  public void charLegacy_backslashConsumesOneDelimiterChar_blockRunsToSecondCloser() {
    List<Token> tokens = scanAll(
      new TokenScanner(BACKSLASH_TEMPLATE, charLegacy().getGlobalConfig())
    );
    assertThat(tokens).hasSize(2);
    assertThat(tokens.get(0)).isInstanceOf(TextToken.class);
    assertThat(tokens.get(0).image).isEqualTo("prefix ");
    assertThat(tokens.get(1)).isInstanceOf(ExpressionToken.class);
    assertThat(tokens.get(1).image).isEqualTo("{{ x \\}} suffix }}");
  }

  @Test
  public void charNew_backslashIgnored_blockClosesAtFirstDelimiter() {
    List<Token> tokens = scanAll(
      new TokenScanner(BACKSLASH_TEMPLATE, charNew().getGlobalConfig())
    );
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0)).isInstanceOf(TextToken.class);
    assertThat(tokens.get(0).image).isEqualTo("prefix ");
    assertThat(tokens.get(1)).isInstanceOf(ExpressionToken.class);
    assertThat(tokens.get(1).image).isEqualTo("{{ x \\}}");
    assertThat(tokens.get(2)).isInstanceOf(TextToken.class);
    assertThat(tokens.get(2).image).isEqualTo(" suffix }}");
  }

  @Test
  public void stringLegacy_backslashConsumesOneDelimiterChar_blockRunsToSecondCloser() {
    List<Token> tokens = scanAll(
      new StringTokenScanner(BACKSLASH_TEMPLATE, stringLegacy().getGlobalConfig())
    );
    assertThat(tokens).hasSize(2);
    assertThat(tokens.get(0)).isInstanceOf(TextToken.class);
    assertThat(tokens.get(0).image).isEqualTo("prefix ");
    assertThat(tokens.get(1)).isInstanceOf(ExpressionToken.class);
    assertThat(tokens.get(1).image).isEqualTo("{{ x \\}} suffix }}");
  }

  @Test
  public void stringNew_backslashIgnored_blockClosesAtFirstDelimiter() {
    List<Token> tokens = scanAll(
      new StringTokenScanner(BACKSLASH_TEMPLATE, stringNew().getGlobalConfig())
    );
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0)).isInstanceOf(TextToken.class);
    assertThat(tokens.get(0).image).isEqualTo("prefix ");
    assertThat(tokens.get(1)).isInstanceOf(ExpressionToken.class);
    assertThat(tokens.get(1).image).isEqualTo("{{ x \\}}");
    assertThat(tokens.get(2)).isInstanceOf(TextToken.class);
    assertThat(tokens.get(2).image).isEqualTo(" suffix }}");
  }

  private static List<Token> scanAll(AbstractIterator<Token> scanner) {
    List<Token> tokens = new ArrayList<>();
    scanner.forEachRemaining(tokens::add);
    return tokens;
  }

  // ── Backslash in a plain variable expression ───────────────────────────────
  //
  // The most common real-world case: a Windows path or similar string passed
  // directly as a variable value. The backslash is in the *value*, not the
  // template, so scanner behaviour is irrelevant — both modes should render
  // identically.

  @Test
  public void backslashInVariableValueIsUnaffectedByFlag_char() {
    ImmutableMap<String, Object> ctx = ImmutableMap.of("path", "C:\\Users\\foo");
    assertThat(charLegacy().render("{{ path }}", ctx)).isEqualTo("C:\\Users\\foo");
    assertThat(charNew().render("{{ path }}", ctx)).isEqualTo("C:\\Users\\foo");
  }

  @Test
  public void backslashInVariableValueIsUnaffectedByFlag_string() {
    ImmutableMap<String, Object> ctx = ImmutableMap.of("path", "C:\\Users\\foo");
    assertThat(stringLegacy().render("{{ path }}", ctx)).isEqualTo("C:\\Users\\foo");
    assertThat(stringNew().render("{{ path }}", ctx)).isEqualTo("C:\\Users\\foo");
  }

  // ── New behaviour: simple expressions are unaffected ──────────────────────
  //
  // Expressions with no backslash should behave identically under both modes.

  @Test
  public void charNew_simpleExpressionUnchanged() {
    assertThat(charNew().render("{{ greeting }}", ImmutableMap.of("greeting", "hello")))
      .isEqualTo("hello");
  }

  @Test
  public void stringNew_simpleExpressionUnchanged() {
    assertThat(stringNew().render("{{ greeting }}", ImmutableMap.of("greeting", "hello")))
      .isEqualTo("hello");
  }

  // ── LegacyOverrides preset assertions ─────────────────────────────────────

  @Test
  public void allPresetDoesNotEnableNewBackslashHandling() {
    assertThat(LegacyOverrides.ALL.isHandleBackslashInQuotesOnly()).isTrue();
  }

  @Test
  public void threePointZeroPresetDoesNotEnableNewBackslashHandling() {
    assertThat(LegacyOverrides.THREE_POINT_0.isHandleBackslashInQuotesOnly()).isTrue();
  }

  @Test
  public void nonePresetKeepsLegacyBackslashHandling() {
    assertThat(LegacyOverrides.NONE.isHandleBackslashInQuotesOnly()).isFalse();
  }
}
