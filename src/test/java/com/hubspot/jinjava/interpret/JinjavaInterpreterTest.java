package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.parse.FixedToken;
import com.hubspot.jinjava.tree.NodeList;
import com.hubspot.jinjava.tree.TextNode;


public class JinjavaInterpreterTest {

  Jinjava jinjava;
  JinjavaInterpreter interpreter;
  
  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, jinjava.getGlobalContext(), jinjava.getGlobalConfig());
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
    interpreter.addBlock("foobar", new NodeList(new TextNode(new FixedToken("sparta", -1))));
    String content = String.format("this is %sfoobar%s!", JinjavaInterpreter.BLOCK_STUB_START, JinjavaInterpreter.BLOCK_STUB_END);
    assertThat(interpreter.resolveBlockStubs(content)).isEqualTo("this is sparta!");
  }
  
  @Test
  public void resolveBlockStubsWithSpecialChars() throws Exception {
    interpreter.addBlock("foobar", new NodeList(new TextNode(new FixedToken("$150.00", -1))));
    String content = String.format("this is %sfoobar%s!", JinjavaInterpreter.BLOCK_STUB_START, JinjavaInterpreter.BLOCK_STUB_END);
    assertThat(interpreter.resolveBlockStubs(content)).isEqualTo("this is $150.00!");
  }
  
}
