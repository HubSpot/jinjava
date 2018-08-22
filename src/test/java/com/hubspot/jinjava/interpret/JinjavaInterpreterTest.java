package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.tree.TextNode;
import com.hubspot.jinjava.tree.parse.TextToken;

public class JinjavaInterpreterTest {

  private Jinjava jinjava;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
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
    interpreter.addBlock("foobar", Lists.newLinkedList(Lists.newArrayList((new TextNode(new TextToken("sparta", -1, -1))))));
    String content = "this is {% block foobar %}foobar{% endblock %}!";
    assertThat(interpreter.render(content)).isEqualTo("this is sparta!");
  }

  @Test
  public void resolveBlockStubsWithSpecialChars() {
    interpreter.addBlock("foobar", Lists.newLinkedList(Lists.newArrayList(new TextNode(new TextToken("$150.00", -1, -1)))));
    String content = "this is {% block foobar %}foobar{% endblock %}!";
    assertThat(interpreter.render(content)).isEqualTo("this is $150.00!");
  }

  @Test
  public void resolveBlockStubsWithCycle() {
    String content = interpreter.render("{% block foo %}{% block foo %}{% endblock %}{% endblock %}");
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
  public void triesBeanMethodFirst() {
    assertThat(interpreter.resolveProperty(ZonedDateTime.parse("2013-09-19T12:12:12+00:00"), "year")
        .toString()).isEqualTo("2013");
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
    assertThat(interpreter.getContext().getDependencies().get(dependencyType)).contains(dependencyIdentifier);
    interpreter.leaveScope();

    assertThat(interpreter.getContext().getDependencies().get(dependencyType)).contains(dependencyIdentifier);
  }

  @Test
  public void parseWithSyntaxError() {
    RenderResult result = new Jinjava().renderForResult("{%}", new HashMap<>());
    assertThat(result.getErrors()).isNotEmpty();
    assertThat(result.getErrors().get(0).getReason()).isEqualTo(ErrorReason.SYNTAX_ERROR);
  }

  @Test
  public void itLimitsOutputSize() {

    JinjavaConfig outputSizeLimitedConfig = JinjavaConfig.newBuilder().withMaxOutputSize(20).build();
    String output = "123456789012345678901234567890";

    RenderResult renderResult = new Jinjava().renderForResult(output, new HashMap<>());
    assertThat(renderResult.getOutput()).isEqualTo(output);
    assertThat(renderResult.hasErrors()).isFalse();

    renderResult = new Jinjava(outputSizeLimitedConfig).renderForResult(output, new HashMap<>());
    assertThat(renderResult.getErrors().get(0).getMessage()).contains("OutputTooBigException");
  }

  @Test
  public void itLimitsOutputSizeWhenSumOfNodeSizesExceedsMax() {
    JinjavaConfig outputSizeLimitedConfig = JinjavaConfig.newBuilder().withMaxOutputSize(19).build();
    String input = "1234567890{% block testchild %}1234567890{% endblock %}";
    String output = "12345678901234567890"; // Note that this exceeds the max size

    RenderResult renderResult = new Jinjava().renderForResult(input, new HashMap<>());
    assertThat(renderResult.getOutput()).isEqualTo(output);
    assertThat(renderResult.hasErrors()).isFalse();

    renderResult = new Jinjava(outputSizeLimitedConfig).renderForResult(input, new HashMap<>());
    assertThat(renderResult.hasErrors()).isTrue();
    assertThat(renderResult.getErrors().get(0).getMessage()).contains("OutputTooBigException");
  }
}
