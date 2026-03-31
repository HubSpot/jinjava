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

  // ── Builder validation ─────────────────────────────────────────────────────

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

  @Test
  public void itStripsLineCommentPrefix() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols.builder().withLineCommentPrefix("%#").build()
    );
    String template = "before\n%# this whole line is a comment\nafter";
    assertThat(j.render(template, new HashMap<>())).isEqualTo("before\nafter");
  }

  @Test
  public void itStripsLineCommentWithLeadingWhitespace() {
    Jinjava j = jinjavaWith(
      StringTokenScannerSymbols.builder().withLineCommentPrefix("%#").build()
    );
    String template = "before\n  %# indented comment\nafter";
    assertThat(j.render(template, new HashMap<>())).isEqualTo("before\nafter");
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
    assertThat(j.render(template, new HashMap<>())).isEqualTo("7");
  }

  // ── Helper ────────────────────────────────────────────────────────────────

  private Jinjava jinjavaWith(StringTokenScannerSymbols symbols) {
    return new Jinjava(
      BaseJinjavaTest.newConfigBuilder().withTokenScannerSymbols(symbols).build()
    );
  }
}
