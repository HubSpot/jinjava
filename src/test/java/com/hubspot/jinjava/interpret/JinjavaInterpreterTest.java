package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.mode.PreserveRawExecutionMode;
import com.hubspot.jinjava.tree.TextNode;
import com.hubspot.jinjava.tree.output.BlockInfo;
import com.hubspot.jinjava.tree.parse.TextToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class JinjavaInterpreterTest {
  private Jinjava jinjava;
  private JinjavaInterpreter interpreter;
  private TokenScannerSymbols symbols;

  @Before
  public void setup() {
    jinjava = new Jinjava();
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
    JinjavaConfig outputSizeLimitedConfig = JinjavaConfig
      .newBuilder()
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
    JinjavaConfig outputSizeLimitedConfig = JinjavaConfig
      .newBuilder()
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
    JinjavaConfig outputSizeLimitedConfig = JinjavaConfig
      .newBuilder()
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
    JinjavaConfig preserveConfig = JinjavaConfig
      .newBuilder()
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
    assertThatThrownBy(
        () ->
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
}
