package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.tree.TextNode;
import com.hubspot.jinjava.tree.parse.TextToken;

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

}
