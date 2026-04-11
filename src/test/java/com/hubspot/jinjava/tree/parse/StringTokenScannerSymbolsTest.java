package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.lib.filter.JoinFilterTest.User;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class StringTokenScannerSymbolsTest {

  // ── Shared symbol configurations ───────────────────────────────────────────

  /** LaTeX-style delimiters as used in the original issue #195 example. */
  private static final StringTokenScannerSymbols LATEX_SYMBOLS = StringTokenScannerSymbols
    .builder()
    .withVariableStartString("\\VAR{")
    .withVariableEndString("}")
    .withBlockStartString("\\BLOCK{")
    .withBlockEndString("}")
    .withCommentStartString("\\#{")
    .withCommentEndString("}")
    .build();

  /** Angle-bracket style — same delimiters as the existing CustomTokenScannerSymbolsTest. */
  private static final StringTokenScannerSymbols ANGLE_SYMBOLS = StringTokenScannerSymbols
    .builder()
    .withVariableStartString("<<")
    .withVariableEndString(">>")
    .withBlockStartString("<%")
    .withBlockEndString("%>")
    .withCommentStartString("<#")
    .withCommentEndString("#>")
    .build();

  private Jinjava latexJinjava;
  private Jinjava angleJinjava;

  @Before
  public void setup() {
    latexJinjava =
      new Jinjava(
        BaseJinjavaTest.newConfigBuilder().withTokenScannerSymbols(LATEX_SYMBOLS).build()
      );
    latexJinjava
      .getGlobalContext()
      .put("numbers", Lists.newArrayList(1L, 2L, 3L, 4L, 5L));

    angleJinjava =
      new Jinjava(
        BaseJinjavaTest.newConfigBuilder().withTokenScannerSymbols(ANGLE_SYMBOLS).build()
      );
    angleJinjava
      .getGlobalContext()
      .put("numbers", Lists.newArrayList(1L, 2L, 3L, 4L, 5L));
  }

  // ── Plain text ─────────────────────────────────────────────────────────────

  @Test
  public void itRendersPlainText() {
    String template = "jinjava interpreter works correctly";
    assertThat(latexJinjava.render(template, new HashMap<>())).isEqualTo(template);
    assertThat(angleJinjava.render(template, new HashMap<>())).isEqualTo(template);
  }

  // ── Variable expressions ───────────────────────────────────────────────────

  @Test
  public void itRendersVariablesWithLatexSymbols() {
    assertThat(latexJinjava.render("\\VAR{ name }", ImmutableMap.of("name", "World")))
      .isEqualTo("World");
  }

  @Test
  public void itRendersVariablesWithAngleSymbols() {
    assertThat(angleJinjava.render("<< name >>", ImmutableMap.of("name", "World")))
      .isEqualTo("World");
  }

  // ── Default delimiters pass through as literal text ────────────────────────

  @Test
  public void itPassesThroughDefaultCurlyBracesAsLiteralText() {
    // With custom delimiters, {{ }} must be treated as plain text, not expressions.
    assertThat(
      latexJinjava.render(
        "{{ not a variable }} \\VAR{ name }",
        ImmutableMap.of("name", "Jorge")
      )
    )
      .isEqualTo("{{ not a variable }} Jorge");

    assertThat(
      angleJinjava.render(
        "{{ not a variable }} << name >>",
        ImmutableMap.of("name", "Jorge")
      )
    )
      .isEqualTo("{{ not a variable }} Jorge");
  }

  // ── Block tags ─────────────────────────────────────────────────────────────

  @Test
  public void itRendersIfBlockWithLatexSymbols() {
    assertThat(
      latexJinjava.render(
        "\\BLOCK{ if show }hello\\BLOCK{ endif }",
        ImmutableMap.of("show", true)
      )
    )
      .isEqualTo("hello");

    assertThat(
      latexJinjava.render(
        "\\BLOCK{ if show }hello\\BLOCK{ endif }",
        ImmutableMap.of("show", false)
      )
    )
      .isEqualTo("");
  }

  @Test
  public void itRendersSetBlockWithAngleSymbols() {
    assertThat(
      angleJinjava.render(
        "<% set d=d | default(\"some random value\") %><< d >>",
        new HashMap<>()
      )
    )
      .isEqualTo("some random value");
  }

  // ── Comments ───────────────────────────────────────────────────────────────

  @Test
  public void itStripsCommentsWithLatexSymbols() {
    assertThat(latexJinjava.render("before\\#{ this is ignored }after", new HashMap<>()))
      .isEqualTo("beforeafter");
  }

  @Test
  public void itStripsCommentsWithAngleSymbols() {
    assertThat(angleJinjava.render("before<# this is ignored #>after", new HashMap<>()))
      .isEqualTo("beforeafter");
  }

  // ── Filters ────────────────────────────────────────────────────────────────

  @Test
  public void itRendersFiltersWithLatexSymbols() {
    assertThat(latexJinjava.render("\\VAR{ [1, 2, 3, 3]|union(null) }", new HashMap<>()))
      .isEqualTo("[1, 2, 3]");
    assertThat(
      latexJinjava.render("\\VAR{ numbers|select('equalto', 3) }", new HashMap<>())
    )
      .isEqualTo("[3]");
  }

  @Test
  public void itRendersFiltersWithAngleSymbols() {
    assertThat(angleJinjava.render("<< [1, 2, 3, 3]|union(null) >>", new HashMap<>()))
      .isEqualTo("[1, 2, 3]");
    assertThat(angleJinjava.render("<< numbers|select('equalto', 3) >>", new HashMap<>()))
      .isEqualTo("[3]");
  }

  @Test
  public void itRendersMapFilterWithLatexSymbols() {
    assertThat(
      latexJinjava.render(
        "\\VAR{ users|map(attribute='username')|join(', ') }",
        ImmutableMap.of(
          "users",
          (Object) Lists.newArrayList(new User("foo"), new User("bar"))
        )
      )
    )
      .isEqualTo("foo, bar");
  }

  @Test
  public void itRendersMapFilterWithAngleSymbols() {
    assertThat(
      angleJinjava.render(
        "<< users|map(attribute='username')|join(', ') >>",
        ImmutableMap.of(
          "users",
          (Object) Lists.newArrayList(new User("foo"), new User("bar"))
        )
      )
    )
      .isEqualTo("foo, bar");
  }

  // ── Delimiter characters inside string literals in expressions ─────────────

  @Test
  public void itHandlesClosingDelimiterInsideQuotedString() {
    // The "}" inside the default string must not prematurely close \VAR{
    assertThat(latexJinjava.render("\\VAR{ name | default(\"}\") }", new HashMap<>()))
      .isEqualTo("}");
  }

  @Test
  public void itHandlesClosingDelimiterInsideQuotedStringAngle() {
    // ">>" inside a quoted string must not close the << expression
    assertThat(angleJinjava.render("<< name | default(\">>\") >>", new HashMap<>()))
      .isEqualTo(">>");
  }

  // ── Builder defaults produce same behaviour as DefaultTokenScannerSymbols ──

  @Test
  public void defaultBuilderBehavesLikeDefaultSymbols() {
    Jinjava defaultJinjava = new Jinjava();
    Jinjava stringBasedDefaultJinjava = new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withTokenScannerSymbols(StringTokenScannerSymbols.builder().build())
        .build()
    );
    String template = "{{ greeting }}, {{ name }}!";
    ImmutableMap<String, Object> ctx = ImmutableMap.of(
      "greeting",
      "Hello",
      "name",
      "World"
    );
    assertThat(stringBasedDefaultJinjava.render(template, ctx))
      .isEqualTo(defaultJinjava.render(template, ctx));
  }

  // ── trimBlocks and lstripBlocks ────────────────────────────────────────────
  //
  // trimBlocks is handled in TokenScanner.emitStringToken(): when a TagToken or
  // NoteToken is emitted and trimBlocks=true, the immediately following newline
  // is consumed. This is equally true in the string-based path.
  //
  // lstripBlocks is handled in TreeParser, which operates on the token stream
  // produced by TokenScanner. It strips leading horizontal whitespace from any
  // TextNode that immediately precedes a TagNode. Since TreeParser is path-agnostic,
  // lstripBlocks works identically for both char-based and string-based scanning.

  @Test
  public void itRespectsTrimBlocksWithAngleSymbols() {
    Jinjava j = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withTokenScannerSymbols(ANGLE_SYMBOLS)
        .withTrimBlocks(true)
        .build()
    );
    // Without trimBlocks the newline after <% if show %> would appear in output.
    // With trimBlocks=true it is consumed by the scanner, so output is "hello".
    String result = j.render(
      "<% if show %>\nhello\n<% endif %>",
      ImmutableMap.of("show", true)
    );
    assertThat(result).isEqualTo("hello\n");
  }

  @Test
  public void itRespectsTrimBlocksWithLatexSymbols() {
    Jinjava j = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withTokenScannerSymbols(LATEX_SYMBOLS)
        .withTrimBlocks(true)
        .build()
    );
    String result = j.render(
      "\\BLOCK{ if show }\nhello\n\\BLOCK{ endif }",
      ImmutableMap.of("show", true)
    );
    assertThat(result).isEqualTo("hello\n");
  }

  @Test
  public void itRespectsLstripBlocksWithAngleSymbols() {
    Jinjava j = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withTokenScannerSymbols(ANGLE_SYMBOLS)
        .withLstripBlocks(true)
        .withTrimBlocks(true)
        .build()
    );
    // Leading spaces before the tag are stripped by lstripBlocks (TreeParser).
    // The newline after the tag is consumed by trimBlocks (TokenScanner).
    String result = j.render(
      "    <% if show %>\nhello\n    <% endif %>",
      ImmutableMap.of("show", true)
    );
    assertThat(result).isEqualTo("hello\n");
  }

  @Test
  public void itRespectsLstripBlocksWithLatexSymbols() {
    Jinjava j = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withTokenScannerSymbols(LATEX_SYMBOLS)
        .withLstripBlocks(true)
        .withTrimBlocks(true)
        .build()
    );
    String result = j.render(
      "    \\BLOCK{ if show }\nhello\n    \\BLOCK{ endif }",
      ImmutableMap.of("show", true)
    );
    assertThat(result).isEqualTo("hello\n");
  }

  @Test
  public void builderRejectsEmptyDelimiter() {
    assertThatThrownBy(() ->
        StringTokenScannerSymbols.builder().withVariableStartString("").build()
      )
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void builderRejectsNullDelimiter() {
    assertThatThrownBy(() ->
        StringTokenScannerSymbols.builder().withBlockEndString(null).build()
      )
      .isInstanceOf(IllegalArgumentException.class);
  }

  // ── Line statement prefix ──────────────────────────────────────────────────

  @Test
  public void itRendersLineStatementPrefix() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols.builder().withLineStatementPrefix("%%").build()
    );
    // "%% if show" is equivalent to "{% if show %}"
    String template = "%% if show\nhello\n%% endif";
    assertThat(j.render(template, ImmutableMap.of("show", true))).isEqualTo("hello\n");
    assertThat(j.render(template, ImmutableMap.of("show", false))).isEqualTo("");
  }

  @Test
  public void itRendersLineStatementPrefixWithWhitespaceControl() {
    Jinjava j = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withTokenScannerSymbols(
          StringTokenScannerSymbols.builder().withLineStatementPrefix("%%").build()
        )
        .withTrimBlocks(true)
        .withLstripBlocks(true)
        .build()
    );
    // "%%- for" strips the newline before the line (leftTrim).
    // trimBlocks consumes the newline after each tag line.
    // Expected: the \n after {| is stripped, c| repeated col_num times, each
    // followed by \n (from the body line), with the \n after c| stripped by
    // the leftTrim on %%- endfor.
    String template = "before|\n%%- for _ in range(3)\nc|\n%%- endfor\nafter";
    assertThat(j.render(template, ImmutableMap.of())).isEqualTo("before|c|c|c|after");
  }

  @Test
  public void itRendersLineStatementPrefixWithLeadingWhitespace() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols.builder().withLineStatementPrefix("%%").build()
    );
    // Leading spaces before the prefix are allowed
    String template = "  %% if show\nhello\n  %% endif";
    assertThat(j.render(template, ImmutableMap.of("show", true))).isEqualTo("hello\n");
  }

  @Test
  public void itRendersLineStatementMixedWithBlockDelimiters() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols
        .builder()
        .withVariableStartString("<<")
        .withVariableEndString(">>")
        .withBlockStartString("<%")
        .withBlockEndString("%>")
        .withCommentStartString("<#")
        .withCommentEndString("#>")
        .withLineStatementPrefix("%%")
        .build()
    );
    String template = "%% set x = 42\n<< x >>";
    assertThat(j.render(template, new HashMap<>())).isEqualTo("42");
  }

  // ── Line comment prefix ────────────────────────────────────────────────────
  //
  // Ground truth confirmed by running both Python Jinja2 and Jinjava against:
  //   [START]
  //   %% set x = 1
  //   [A]
  //   %# plain comment
  //   [B]
  //   %#- trim comment
  //   [C]
  //   %% set y = 2
  //   [D]
  //   [END]
  //
  // Python output: [START]\n[A]\n\n[B]\n[C]\n[D]\n[END]
  //
  // Semantics:
  //   %#  (plain): comment content stripped, trailing \n KEPT  → blank line where comment was
  //   %#- (trim):  comment content AND trailing \n stripped     → no blank line
  //   Neither form affects the newline that ended the preceding line.

  @Test
  public void itStripsLineCommentPrefixLeavingBlankLine() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols.builder().withLineCommentPrefix("%#").build()
    );
    // %# keeps its trailing \n → "before\n" + "\n" (comment's own \n) + "after"
    String template = "before\n%# this whole line is a comment\nafter";
    assertThat(j.render(template, new HashMap<>())).isEqualTo("before\n\nafter");
  }

  @Test
  public void itStripsLineCommentWithLeadingWhitespace() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols.builder().withLineCommentPrefix("%#").build()
    );
    // Indentation before %# is stripped, trailing \n is kept → blank line
    String template = "before\n  %# indented comment\nafter";
    assertThat(j.render(template, new HashMap<>())).isEqualTo("before\n\nafter");
  }

  @Test
  public void itStripsLineCommentWithTrimModifier() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols.builder().withLineCommentPrefix("%#").build()
    );
    // %#  keeps trailing \n (blank line left in output)
    assertThat(j.render("before\n%# comment\nafter", new HashMap<>()))
      .isEqualTo("before\n\nafter");
    // %#- also keeps trailing \n — the '-' is LEFT-trim only (strips preceding blanks)
    // With no preceding blank lines, result is identical to plain %#
    assertThat(j.render("before\n%#- comment\nafter", new HashMap<>()))
      .isEqualTo("before\nafter");
    // %#- with a preceding blank line: strips the blank, keeps own trailing \n
    assertThat(j.render("before\n\n%#- comment\nafter", new HashMap<>()))
      .isEqualTo("before\nafter");
  }

  @Test
  public void itStripsLineCommentWithoutLeavingBlankLine() {
    // %#- with real content before (no blank): strips the preceding \n,
    // keeps comment's own \n. "\\begin{document}" + "\n" (comment's \n) + "\\section*{...}"
    Jinjava j = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withTokenScannerSymbols(
          StringTokenScannerSymbols
            .builder()
            .withVariableStartString("\\VAR{")
            .withVariableEndString("}")
            .withLineCommentPrefix("%#")
            .build()
        )
        .build()
    );
    String template =
      "\\begin{document}\n%#-\\VAR{reportHeader}\n\\section*{\\VAR{title}}";
    String result = j.render(template, ImmutableMap.of("title", "My Report"));
    assertThat(result).isEqualTo("\\begin{document}\n\\section*{My Report}");
  }

  @Test
  public void itHandlesBothLinePrefixesTogether() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols
        .builder()
        .withVariableStartString("<<")
        .withVariableEndString(">>")
        .withBlockStartString("<%")
        .withBlockEndString("%>")
        .withCommentStartString("<#")
        .withCommentEndString("#>")
        .withLineStatementPrefix("%%")
        .withLineCommentPrefix("%#")
        .build()
    );
    String template = "%# this is stripped\n%% set x = 7\n<< x >>";
    // %# keeps its trailing \n → blank line, then %% set produces nothing,
    // then << x >> renders as 7. Result: "\n7"
    assertThat(j.render(template, new HashMap<>())).isEqualTo("\n7");
  }

  // ── Helper ────────────────────────────────────────────────────────────────

  private Jinjava jinjavaWith(StringTokenScannerSymbols symbols) {
    return new Jinjava(
      BaseJinjavaTest.newConfigBuilder().withTokenScannerSymbols(symbols).build()
    );
  }
}
