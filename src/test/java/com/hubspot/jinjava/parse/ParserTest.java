/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.parse;

import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_ECHO;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_FIXED;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_NOTE;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
//import org.junit.Ignore;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ParserTest {

  JinjavaInterpreter interpreter;
  TokenParser parser;
  String script;
  
  @Before
  public void setup() {
    Jinjava jinjava = new Jinjava();
    Context context = new Context();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
  }

  @Test @Ignore("most likely unsupported behavior, not needed in jinja")
  public void test1() {
    script = "{{abc.b}}{% if x %}{\\{abc}}{%endif%}";
    parser = new TokenParser(interpreter, script);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("{{abc}}", parser.next().content.trim());
    assertEquals("{%endif%}", parser.next().image);
  }

  @Test
  public void test2() {
    script = "{{abc.b}}{% if x %}{{abc{%endif";
    parser = new TokenParser(interpreter, script);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("{{abc{%endif", parser.next().content.trim());
  }

  @Test
  public void test3() {
    script = "{{abc.b}}{% if x %}{{{abc}}{%endif%}";
    parser = new TokenParser(interpreter, script);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    Token tk = parser.next();
    assertEquals("{{{abc}}", tk.image);
    assertEquals(TOKEN_ECHO, tk.getType());
    assertEquals("{%endif%}", parser.next().image);
  }

  @Test
  public void test4() {
    script = "{{abc.b}}{% if x %}{{!abc}}{%endif%}";
    parser = new TokenParser(interpreter, script);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    Token tk = parser.next();
    assertEquals("{{!abc}}", tk.image);
    assertEquals(TOKEN_ECHO, tk.getType());
    assertEquals("{%endif%}", parser.next().image);
  }

  @Test
  public void test5() {
    script = "{{abc.b}}{% if x %}a{{abc}\\}{%endif%}";
    parser = new TokenParser(interpreter, script);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a", parser.next().content.trim());
    assertEquals("{{abc}\\}{%endif%}", parser.next().content.trim());
  }

  @Test @Ignore("most likely unsupported behavior unnecessary in jinja")
  public void test6() {
    script = "a{{abc.b}}{% if x 	%}a{\\{abc}}{%endif%}";
    parser = new TokenParser(interpreter, script);
    assertEquals("a", parser.next().image);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a{{abc}}", parser.next().content.trim());
    assertEquals("{%endif%}", parser.next().image);
  }

  @Test
  public void test7() {
    script = "a{{abc.b}}{% if x 	%}a{{abc!}#}%}}}{%endif";
    parser = new TokenParser(interpreter, script);
    assertEquals("a", parser.next().image);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a", parser.next().content.trim());
    assertEquals("{{abc!}#}%}}", parser.next().image);
    assertEquals("}", parser.next().content.trim());
    assertEquals(TOKEN_FIXED, parser.next().getType());
  }

  @Test
  public void test8() {
    script = "a{{abc.b}}{% if x 	%}a{{abc}}{%endif{{";
    parser = new TokenParser(interpreter, script);
    assertEquals("a", parser.next().image);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a", parser.next().content.trim());
    assertEquals(TOKEN_ECHO, parser.next().getType());
    assertEquals("{%endif{{", parser.next().content.trim());
  }

  @Test
  public void test9() {
    script = "a{{abc.b}}{% if x 	%}a{{abc}\\}{%endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("a", parser.next().image);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a", parser.next().content.trim());
    assertEquals(TOKEN_FIXED, parser.next().getType());
  }

  @Test
  public void test10() {
    script = "a{{abc.b}}{% if x %}a{{abc}\\}{{#%endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("a", parser.next().image);
    assertEquals("{{abc.b}}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a", parser.next().content.trim());
    assertEquals("{{abc}\\}{", parser.next().image);
    assertEquals(TOKEN_NOTE, parser.next().getType());
  }

  @Test
  public void test11() {
    script = "a{#abc.b#}{% if x %}a{{abc}\\}{{{{#endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("a", parser.next().image);
    assertEquals("{#abc.b#}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a", parser.next().content.trim());
    assertEquals("{{abc}\\}{{{", parser.next().content.trim());
    assertEquals("{#endif{", parser.next().image);
  }

  @Test
  public void test12() {
    script = "{#abc.b#}{% if x %}a{{abc}\\}{{{{#endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("{#abc.b#}", parser.next().image);
    assertEquals("if x", parser.next().content.trim());
    assertEquals("a", parser.next().content.trim());
    assertEquals("{{abc}\\}{{{", parser.next().content.trim());
    assertEquals(TOKEN_NOTE, parser.next().getType());
  }

  @Test
  public void test13() {
    script = "{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("{#abc{#.b#}", parser.next().image);
  }

  @Test
  public void test14() {
    script = "abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("abc", parser.next().image);
    assertEquals("{#.b#}", parser.next().image);
    assertEquals("{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}", parser.next().image);
  }

  @Test
  public void test15() {
    script = "abc{#.b#}{#xy{!ad!}{#DD#}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("abc", parser.next().image);
    assertEquals("{#.b#}", parser.next().image);
    assertEquals("{#xy{!ad!}{#DD#}", parser.next().image);
  }

  @Test
  public void test16() {
    script = "{#{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("{#{#abc{#.b#}", parser.next().image);
  }

  @Test
  public void test17() {
    script = "{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}#}a#}{{abc}\\}#}{{{{#endif{";
    parser = new TokenParser(interpreter, script);
    assertEquals("{#abc{#.b#}", parser.next().image);
  }
}
