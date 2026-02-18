package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.mode.PreserveRawExecutionMode;
import com.hubspot.jinjava.objects.date.FormattedDate;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import com.hubspot.jinjava.tree.TextNode;
import com.hubspot.jinjava.tree.output.BlockInfo;
import com.hubspot.jinjava.tree.output.OutputList;
import com.hubspot.jinjava.tree.parse.TextToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class JinjavaInterpreterTest {

  private Jinjava jinjava;
  private JinjavaInterpreter interpreter;
  private TokenScannerSymbols symbols;

  @Before
  public void setup() {
    jinjava =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withTimeZone(ZoneId.of("America/New_York"))
          .build()
      );
    interpreter = jinjava.newInterpreter();
    symbols = interpreter.getConfig().getTokenScannerSymbols();
  }

  @Test
  public void resolveBlockStubsWithNoStubs() {
    assertThat(interpreter.render("foo")).isEqualTo("foo");
  }

  @Test
  public void resolveBlockStubsWithMissingNamedBlock() {
    String content = "this is {% block foobar %}{% endblock %}!";
    assertThat(interpreter.render(content)).isEqualTo("this is !");
  }

  @Test
  public void resolveBlockStubs() {
    interpreter.addBlock(
      "foobar",
      new BlockInfo(
        Lists.newLinkedList(
          Lists.newArrayList((new TextNode(new TextToken("sparta", -1, -1, symbols))))
        ),
        Optional.empty(),
        0,
        0
      )
    );
    String content = "this is {% block foobar %}foobar{% endblock %}!";
    assertThat(interpreter.render(content)).isEqualTo("this is sparta!");
  }

  @Test
  public void resolveBlockStubsWithSpecialChars() {
    interpreter.addBlock(
      "foobar",
      new BlockInfo(
        Lists.newLinkedList(
          Lists.newArrayList(new TextNode(new TextToken("$150.00", -1, -1, symbols)))
        ),
        Optional.empty(),
        0,
        0
      )
    );
    String content = "this is {% block foobar %}foobar{% endblock %}!";
    assertThat(interpreter.render(content)).isEqualTo("this is $150.00!");
  }

  @Test
  public void resolveBlockStubsWithCycle() {
    String content = interpreter.render(
      "{% block foo %}{% block foo %}{% endblock %}{% endblock %}"
    );
    assertThat(content).isEmpty();
  }

  // Ex VariableChain stuff

  static class Foo {

    private String bar;

    public Foo(String bar) {
      this.bar = bar;
    }

    public String getBar() {
      return bar;
    }

    public String getBarFoo() {
      return bar;
    }

    public String getBarFoo1() {
      return bar;
    }

    @JsonIgnore
    public String getBarHidden() {
      return bar;
    }
  }

  @Test
  public void singleWordProperty() {
    assertThat(interpreter.resolveProperty(new Foo("a"), "bar")).isEqualTo("a");
  }

  @Test
  public void multiWordCamelCase() {
    assertThat(interpreter.resolveProperty(new Foo("a"), "barFoo")).isEqualTo("a");
  }

  @Test
  public void multiWordSnakeCase() {
    assertThat(interpreter.resolveProperty(new Foo("a"), "bar_foo")).isEqualTo("a");
  }

  @Test
  public void multiWordNumberSnakeCase() {
    assertThat(interpreter.resolveProperty(new Foo("a"), "bar_foo_1")).isEqualTo("a");
  }

  @Test
  public void jsonIgnore() {
    assertThat(interpreter.resolveProperty(new Foo("a"), "barHidden")).isEqualTo("a");
  }

  @Test
  public void triesBeanMethodFirst() {
    assertThat(
      interpreter
        .resolveProperty(ZonedDateTime.parse("2013-09-19T12:12:12+00:00"), "year")
        .toString()
    )
      .isEqualTo("2013");
  }

  @Test
  public void enterScopeTryFinally() {
    interpreter.getContext().put("foo", "parent");

    interpreter.enterScope();
    try {
      interpreter.getContext().put("foo", "child");
      assertThat(interpreter.resolveELExpression("foo", 1)).isEqualTo("child");
    } finally {
      interpreter.leaveScope();
    }

    assertThat(interpreter.resolveELExpression("foo", 1)).isEqualTo("parent");
  }

  @Test
  public void enterScopeTryWithResources() {
    interpreter.getContext().put("foo", "parent");

    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      interpreter.getContext().put("foo", "child");
      assertThat(interpreter.resolveELExpression("foo", 1)).isEqualTo("child");
    }

    assertThat(interpreter.resolveELExpression("foo", 1)).isEqualTo("parent");
  }

  @Test
  public void bubbleUpDependenciesFromLowerScope() {
    String dependencyType = "foo";
    String dependencyIdentifier = "123";

    interpreter.enterScope();
    interpreter.getContext().addDependency(dependencyType, dependencyIdentifier);
    assertThat(interpreter.getContext().getDependencies().get(dependencyType))
      .contains(dependencyIdentifier);
    interpreter.leaveScope();

    assertThat(interpreter.getContext().getDependencies().get(dependencyType))
      .contains(dependencyIdentifier);
  }

  @Test
  public void parseWithSyntaxError() {
    RenderResult result = new Jinjava().renderForResult("{%}", new HashMap<>());
    assertThat(result.getErrors()).isNotEmpty();
    assertThat(result.getErrors().get(0).getReason()).isEqualTo(ErrorReason.SYNTAX_ERROR);
  }

  @Test
  public void itLimitsOutputSize() {
    JinjavaConfig outputSizeLimitedConfig = BaseJinjavaTest
      .newConfigBuilder()
      .withMaxOutputSize(20)
      .build();
    String output = "123456789012345678901234567890";

    RenderResult renderResult = new Jinjava().renderForResult(output, new HashMap<>());
    assertThat(renderResult.getOutput()).isEqualTo(output);
    assertThat(renderResult.hasErrors()).isFalse();

    renderResult =
      new Jinjava(outputSizeLimitedConfig).renderForResult(output, new HashMap<>());
    assertThat(renderResult.getErrors().get(0).getMessage())
      .contains("OutputTooBigException");
  }

  @Test
  public void itLimitsOutputSizeOnTagNode() {
    JinjavaConfig outputSizeLimitedConfig = BaseJinjavaTest
      .newConfigBuilder()
      .withMaxOutputSize(10)
      .build();
    String output = "{% for i in range(20) %} {{ i }} {% endfor %}";

    RenderResult renderResult = new Jinjava().renderForResult(output, new HashMap<>());
    assertThat(renderResult.getOutput())
      .isEqualTo(
        " 0  1  2  3  4  5  6  7  8  9  10  11  12  13  14  15  16  17  18  19 "
      );
    assertThat(renderResult.hasErrors()).isFalse();

    renderResult =
      new Jinjava(outputSizeLimitedConfig).renderForResult(output, new HashMap<>());
    assertThat(renderResult.getErrors().get(0).getMessage())
      .contains("OutputTooBigException");

    assertThat(renderResult.getOutput()).isEqualTo(" 0  1  2  ");
  }

  @Test
  public void itLimitsOutputSizeWhenSumOfNodeSizesExceedsMax() {
    JinjavaConfig outputSizeLimitedConfig = BaseJinjavaTest
      .newConfigBuilder()
      .withMaxOutputSize(19)
      .build();
    String input = "1234567890{% block testchild %}1234567890{% endblock %}";
    String output = "12345678901234567890"; // Note that this exceeds the max size

    RenderResult renderResult = new Jinjava().renderForResult(input, new HashMap<>());
    assertThat(renderResult.getOutput()).isEqualTo(output);
    assertThat(renderResult.hasErrors()).isFalse();

    renderResult =
      new Jinjava(outputSizeLimitedConfig).renderForResult(input, new HashMap<>());
    assertThat(renderResult.hasErrors()).isTrue();
    assertThat(renderResult.getErrors().get(0).getMessage())
      .contains("OutputTooBigException");
  }

  @Test
  public void itCanPreserveRawTags() {
    JinjavaConfig preserveConfig = BaseJinjavaTest
      .newConfigBuilder()
      .withExecutionMode(PreserveRawExecutionMode.instance())
      .build();
    String input = "1{% raw %}2{% endraw %}3";
    String normalOutput = "123";
    String preservedOutput = "1{% raw %}2{% endraw %}3";

    RenderResult renderResult = new Jinjava().renderForResult(input, new HashMap<>());
    assertThat(renderResult.getOutput()).isEqualTo(normalOutput);
    assertThat(renderResult.hasErrors()).isFalse();

    renderResult = new Jinjava(preserveConfig).renderForResult(input, new HashMap<>());
    assertThat(renderResult.getOutput()).isEqualTo(preservedOutput);
    assertThat(renderResult.hasErrors()).isFalse();
  }

  @Test
  public void itKnowsThatMethodIsResolved() {
    // Tests fix of bug where an error when an AstMethod is called would cause an error to be output
    // saying the method could not be resolved.
    String input =
      "{% set a, b = {}, [] %}{% macro a.foo()%} 1-{{ b.bar() }}. {% endmacro %} {{ a.foo() }}";

    RenderResult renderResult = new Jinjava()
      .renderForResult(input, ImmutableMap.of("deferred", DeferredValue.instance()));
    assertThat(renderResult.getOutput().trim()).isEqualTo("1-.");
    // Does not contain an error about 'a.foo()' being unknown.
    assertThat(renderResult.getErrors()).hasSize(1);
  }

  @Test
  public void itThrowsFatalErrors() {
    interpreter.getContext().setThrowInterpreterErrors(true);
    assertThatThrownBy(() ->
        interpreter.addError(
          new TemplateError(
            ErrorType.FATAL,
            ErrorReason.UNKNOWN,
            ErrorItem.PROPERTY,
            "",
            "",
            interpreter.getLineNumber(),
            interpreter.getPosition(),
            new RuntimeException()
          )
        )
      )
      .isInstanceOf(TemplateSyntaxException.class);
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itHidesWarningErrors() {
    interpreter.getContext().setThrowInterpreterErrors(true);
    interpreter.addError(
      new TemplateError(
        ErrorType.WARNING,
        ErrorReason.UNKNOWN,
        ErrorItem.PROPERTY,
        "",
        "",
        interpreter.getLineNumber(),
        interpreter.getPosition(),
        new RuntimeException()
      )
    );
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itBindsUnaryMinusTighterThanCmp() {
    assertThat(interpreter.render("{{ (-5 > 4) }}")).isEqualTo("false");
  }

  @Test
  public void itInterpretsFilterChainsInOrder() {
    assertThat(interpreter.render("{{ 'foo' | upper | replace('O', 'A') }}"))
      .isEqualTo("FAA");
  }

  @Test
  public void itInterpretsWhitespaceControl() {
    assertThat(interpreter.render(".  {%- set x = 5 -%}  .")).isEqualTo("..");
  }

  @Test
  public void itInterpretsEmptyExpressions() {
    assertThat(interpreter.render("{{}}")).isEqualTo("");
  }

  @Test
  public void itInterpretsFormattedDates() {
    String result = jinjava.render(
      "{{ d }}",
      ImmutableMap.of(
        "d",
        new FormattedDate(
          "medium",
          "en-US",
          ZonedDateTime.of(2022, 10, 20, 17, 9, 43, 0, ZoneId.of("America/New_York"))
        )
      )
    );

    assertThat(result).isIn("Oct 20, 2022, 5:09:43 PM", "Oct 20, 2022, 5:09:43 PM");
  }

  @Test
  public void itHandlesInvalidFormatInFormattedDate() {
    RenderResult result = jinjava.renderForResult(
      "{{ d }}",
      ImmutableMap.of(
        "d",
        new FormattedDate(
          "not a real format",
          "en_US",
          ZonedDateTime.of(2022, 10, 20, 17, 9, 43, 0, ZoneId.of("America/New_York"))
        )
      )
    );

    assertThat(result.getErrors())
      .extracting(TemplateError::getMessage)
      .containsOnly("Invalid date format 'not a real format'");
  }

  @Test
  public void itDefaultsToMediumOnEmptyFormatInFormattedDate() {
    ZonedDateTime date = ZonedDateTime.of(
      2022,
      10,
      20,
      17,
      9,
      43,
      0,
      ZoneId.of("America/New_York")
    );
    String result = jinjava.render(
      "{{ d }}",
      ImmutableMap.of("d", new FormattedDate("", "en_US", date))
    );

    assertThat(result)
      .isEqualTo(
        StrftimeFormatter.format(date, "medium", Locale.forLanguageTag("en-US"))
      );
  }

  @Test
  public void itHandlesInvalidLocaleInFormattedDate() {
    RenderResult result = jinjava.renderForResult(
      "{{ d }}",
      ImmutableMap.of(
        "d",
        new FormattedDate(
          "medium",
          "not a real locale",
          ZonedDateTime.of(2022, 10, 20, 17, 9, 43, 0, ZoneId.of("America/New_York"))
        )
      )
    );

    assertThat(result.getErrors())
      .extracting(TemplateError::getMessage)
      .containsOnly("Invalid locale format: not a real locale");
  }

  @Test
  public void itDefaultsToUnitedStatesOnEmptyLocaleInFormattedDate() {
    ZonedDateTime date = ZonedDateTime.of(
      2022,
      10,
      20,
      17,
      9,
      43,
      0,
      ZoneId.of("America/New_York")
    );
    String result = jinjava.render(
      "{{ d }}",
      ImmutableMap.of("d", new FormattedDate("medium", "", date))
    );

    assertThat(result)
      .isEqualTo(
        StrftimeFormatter.format(date, "medium", Locale.forLanguageTag("en-US"))
      );
  }

  @Test
  public void itFiltersDuplicateErrors() {
    TemplateError error1 = new TemplateError(
      TemplateError.ErrorType.WARNING,
      TemplateError.ErrorReason.OTHER,
      TemplateError.ErrorItem.FILTER,
      "the first error",
      "list",
      interpreter.getLineNumber(),
      interpreter.getPosition(),
      null
    );

    TemplateError copiedError1 = new TemplateError(
      TemplateError.ErrorType.WARNING,
      TemplateError.ErrorReason.OTHER,
      TemplateError.ErrorItem.FILTER,
      "the first error",
      "list",
      interpreter.getLineNumber(),
      interpreter.getPosition(),
      null
    );

    TemplateError error2 = new TemplateError(
      TemplateError.ErrorType.WARNING,
      TemplateError.ErrorReason.OTHER,
      TemplateError.ErrorItem.FILTER,
      "the second error",
      "list",
      interpreter.getLineNumber(),
      interpreter.getPosition(),
      null
    );

    interpreter.addError(error1);
    interpreter.addError(error2);
    interpreter.addError(copiedError1);

    assertThat(interpreter.getErrors()).containsExactly(error1, error2);
  }

  @Test
  public void itPreventsAccidentalExpressions() {
    String makeExpression = "if (true) {\n{%- print deferred -%}\n}";
    String makeTag = "if (true) {\n{%- print '% print 123 %' -%}\n}";
    String makeNote = "if (true) {\n{%- print '# note #' -%}\n}";
    jinjava.getGlobalContext().put("deferred", DeferredValue.instance());

    JinjavaInterpreter normalInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContext(),
      BaseJinjavaTest
        .newConfigBuilder()
        .withExecutionMode(EagerExecutionMode.instance())
        .build()
    );
    JinjavaInterpreter preventingInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContext(),
      BaseJinjavaTest
        .newConfigBuilder()
        .withFeatureConfig(
          FeatureConfig
            .newBuilder()
            .add(OutputList.PREVENT_ACCIDENTAL_EXPRESSIONS, FeatureStrategies.ACTIVE)
            .build()
        )
        .withExecutionMode(EagerExecutionMode.instance())
        .build()
    );
    JinjavaInterpreter.pushCurrent(normalInterpreter);
    try {
      assertThat(normalInterpreter.render(makeExpression))
        .isEqualTo("if (true) {{% print deferred %}}");
      assertThat(normalInterpreter.render(makeTag))
        .isEqualTo("if (true) {% print 123 %}");
      assertThat(normalInterpreter.render(makeNote)).isEqualTo("if (true) {# note #}");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
    JinjavaInterpreter.pushCurrent(preventingInterpreter);
    try {
      assertThat(preventingInterpreter.render(makeExpression))
        .isEqualTo("if (true) {\n" + "{#- #}{% print deferred %}}");
      assertThat(preventingInterpreter.render(makeTag))
        .isEqualTo("if (true) {\n" + "{#- #}% print 123 %}");
      assertThat(preventingInterpreter.render(makeNote))
        .isEqualTo("if (true) {\n" + "{#- #}# note #}");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itOutputsUndefinedVariableError() {
    String template = "{% set foo=123 %}{{ foo }}{{ bar }}";

    JinjavaInterpreter normalInterpreter = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContext(),
      BaseJinjavaTest
        .newConfigBuilder()
        .withExecutionMode(EagerExecutionMode.instance())
        .build()
    );
    JinjavaInterpreter outputtingErrorInterpreters = new JinjavaInterpreter(
      jinjava,
      jinjava.getGlobalContext(),
      BaseJinjavaTest
        .newConfigBuilder()
        .withFeatureConfig(
          FeatureConfig
            .newBuilder()
            .add(
              JinjavaInterpreter.OUTPUT_UNDEFINED_VARIABLES_ERROR,
              FeatureStrategies.ACTIVE
            )
            .build()
        )
        .withExecutionMode(EagerExecutionMode.instance())
        .build()
    );

    String normalRenderResult = normalInterpreter.render(template);
    String outputtingErrorRenderResult = outputtingErrorInterpreters.render(template);
    assertThat(normalRenderResult).isEqualTo("123");
    assertThat(outputtingErrorRenderResult).isEqualTo("123");
    assertThat(normalInterpreter.getErrors()).isEmpty();
    assertThat(outputtingErrorInterpreters.getErrors().size()).isEqualTo(1);
    assertThat(outputtingErrorInterpreters.getErrors().get(0).getMessage())
      .contains("Undefined variable: 'bar'");
    assertThat(outputtingErrorInterpreters.getErrors().get(0).getReason())
      .isEqualTo(ErrorReason.UNKNOWN);
    assertThat(outputtingErrorInterpreters.getErrors().get(0).getSeverity())
      .isEqualTo(ErrorType.WARNING);
    assertThat(outputtingErrorInterpreters.getErrors().get(0).getCategoryErrors())
      .isEqualTo(ImmutableMap.of("variable", "bar"));
  }

  @Test
  public void itDoesNotAllowAccessingPropertiesOfInterpreter() {
    assertThat(jinjava.render("{{ null.config }}", new HashMap<>())).isEqualTo("");
  }
}
