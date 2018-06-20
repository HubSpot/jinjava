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
package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;

public class TagTest {

  Jinjava jinjava;

  String script;
  Map<String, Object> bindings;
  Object res;

  @Before
  public void setup() {
    jinjava = new Jinjava();

    bindings = new HashMap<>();
    bindings.put("var1", new Integer[] { 23, 45, 45, 689 });
    bindings.put("var2", "45");
    bindings.put("var3", 12);
    bindings.put("var5", "");
  }

  @Test
  public void forTag() {
    script = "{% for item in var1 %}{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("234545689", res);
  }

  @Test
  public void forTag1() {
    script = "{% for  %}{{item}}{% endfor%}";
    RenderResult r = jinjava.renderForResult(script, bindings);
    assertThat(r.getErrors()).hasSize(1);
    assertThat(r.getErrors().get(0).getReason()).isEqualTo(ErrorReason.SYNTAX_ERROR);
  }

  @Test
  public void forLoop() {
    script = "{% for item in var1 %}{{loop.first}}{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("true23false45false45false689", res);
  }

  @Test
  public void forLoop1() {
    script = "{% for item in var1 %}{{loop.last}}{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("false23false45false45true689", res);
  }

  @Test
  public void forLoop2() {
    script = "{% for item in var1 %}{{loop.index0}}-{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("0-231-452-453-689", res);
  }

  @Test
  public void forLoop3() {
    script = "{% for item in var1 %}{{loop.counter}}-{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("1-232-453-454-689", res);
  }

  @Test
  public void forLoop4() {
    script = "{% for item in var1 %}{{loop.revindex0}}-{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("3-232-451-450-689", res);
  }

  @Test
  public void forLoop5() {
    String s = "{% for item in var2|list %}<li>{{ item }}</li>{% endfor %}";
    res = jinjava.render(s, bindings);
    assertEquals("<li>4</li><li>5</li>", res);
  }

  @Test
  public void reverseFor() {
    script = "{% for item in var1|reverse %}{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("689454523", res);
  }

  @Test
  public void ifchangedFor() {
    script = "{% for item in var1|reverse %}{%ifchanged item%}{{item}}{%endifchanged%}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("6894523", res);
  }

  @Test
  public void cycleFor() {
    script = "{% for item in var1 %}{% cycle 'a','b','c'%}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("abca", res);
  }

  @Test
  public void cycleFor0() {
    script = "{% for item in var1 %}{% cycle var3,var2,'hello'%}{{item}}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("12234545hello4512689", res);
  }

  @Test
  public void cycleFor1() {
    script = "{% cycle 'a','b','c' as d%}{% for item in var1 %}{%cycle d%}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("abca", res);
  }

  @Test
  public void cycleFor2() {
    script = "{% cycle var3,var2,'hello' as d%}{% for item in var1 %}{%cycle d%}{% endfor%}";
    res = jinjava.render(script, bindings);
    assertEquals("1245hello12", res);
  }

  @Test
  public void cycle() {
    script = "{% cycle var3,var2,'hello'%}";
    res = jinjava.render(script, bindings);
    assertEquals("12", res);
  }

  @Test
  public void ifTag() {
    script = "{%if var1 %}hello{%endif%}";
    res = jinjava.render(script, bindings);
    assertEquals("hello", res);
  }

  @Test
  public void ifTag0() {
    script = "{%if var4 %}hello{%endif%}";
    res = jinjava.render(script, bindings);
    assertEquals("", res);
  }

  @Test
  public void ifTag1() {
    script = "{%if var5 %}hello{%else%}world{%endif%}";
    res = jinjava.render(script, bindings);
    assertEquals("world", res);
  }

  @Test
  public void ifTag2() {
    script = "{%if var2 == 45 %}hello{%else%}world{%endif%}";
    res = jinjava.render(script, bindings);
    assertEquals("hello", res);
  }

  @Test
  public void ifTag3() {
    script = "{%if var3 == '12' %}hello{%else%}world{%endif%}";
    res = jinjava.render(script, bindings);
    assertEquals("hello", res);
  }

  @Test
  public void ifTag6() {
    script = "{%if var1 and var5 %}hello{%else%}world{%endif%}";
    res = jinjava.render(script, bindings);
    assertEquals("world", res);
  }

  @Test
  public void ifTag7() {
    script = "{%if var1 or var5 %}hello{%else%}world{%endif%}";
    res = jinjava.render(script, bindings);
    assertEquals("hello", res);
  }

  @Test
  public void ifTag8() {
    script = "{%if  %}hello{%else%}world{%endif%}";
    RenderResult r = jinjava.renderForResult(script, bindings);
    assertThat(r.getErrors()).hasSize(1);
    assertThat(r.getErrors().get(0).getReason()).isEqualTo(ErrorReason.SYNTAX_ERROR);
  }

  @Test
  public void echo1() {
    script = "{{ 	 }}";
    res = jinjava.render(script, bindings);
    assertEquals("", res);
  }

  @Test
  public void echo2() {
    script = "{{	_	}}";
    res = jinjava.render(script, bindings);
    assertEquals("", res);
  }

  @Test(expected = InterpretException.class)
  public void block() {
    script = "{%block 	%}";
    res = jinjava.render(script, bindings);
    assertEquals("", res);
  }

  @Test(expected = InterpretException.class)
  public void block1() {
    script = "{%block a	%}";
    res = jinjava.render(script, bindings);
    assertEquals("", res);
  }
}
