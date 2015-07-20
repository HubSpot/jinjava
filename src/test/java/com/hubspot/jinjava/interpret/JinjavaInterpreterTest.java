package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.tree.TextNode;
import com.hubspot.jinjava.tree.parse.TextToken;

import java.time.ZonedDateTime;

public class JinjavaInterpreterTest {

  Jinjava jinjava;
  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
  }

  @Test
  public void resolveBlockStubsWithNoStubs() {
    assertThat(interpreter.resolveBlockStubs("foo")).isEqualTo("foo");
  }

  @Test
  public void resolveBlockStubsWithMissingNamedBlock() {
    String content = String.format("this is %sfoobar%s!", JinjavaInterpreter.BLOCK_STUB_START, JinjavaInterpreter.BLOCK_STUB_END);
    assertThat(interpreter.resolveBlockStubs(content)).isEqualTo("this is !");
  }

  @Test
  public void resolveBlockStubs() throws Exception {
    interpreter.addBlock("foobar", Lists.newLinkedList(Lists.newArrayList((new TextNode(new TextToken("sparta", -1))))));
    String content = String.format("this is %sfoobar%s!", JinjavaInterpreter.BLOCK_STUB_START, JinjavaInterpreter.BLOCK_STUB_END);
    assertThat(interpreter.resolveBlockStubs(content)).isEqualTo("this is sparta!");
  }

  @Test
  public void resolveBlockStubsWithSpecialChars() throws Exception {
    interpreter.addBlock("foobar", Lists.newLinkedList(Lists.newArrayList(new TextNode(new TextToken("$150.00", -1)))));
    String content = String.format("this is %sfoobar%s!", JinjavaInterpreter.BLOCK_STUB_START, JinjavaInterpreter.BLOCK_STUB_END);
    assertThat(interpreter.resolveBlockStubs(content)).isEqualTo("this is $150.00!");
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
    assertThat(interpreter.resolveProperty(ZonedDateTime.parse("2013-09-19T12:12:12+00:00"), "year").toString()).isEqualTo("2013");
  }

}
