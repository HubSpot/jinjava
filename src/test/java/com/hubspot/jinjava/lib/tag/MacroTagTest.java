package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;

public class MacroTagTest {

  Context context;
  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);

    context = interpreter.getContext();
  }

  @After
  public void cleanup() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void testSimpleFn() {
    TagNode t = fixture("simple");
    assertThat(t.render(interpreter).getValue()).isEmpty();

    MacroFunction fn = (MacroFunction) interpreter.resolveObject("__macros__.getPath", -1, -1);
    assertThat(fn.getName()).isEqualTo("getPath");
    assertThat(fn.getArguments()).isEmpty();
    assertThat(fn.isCaller()).isFalse();
    assertThat(fn.isCatchKwargs()).isFalse();
    assertThat(fn.isCatchVarargs()).isFalse();

    context.put("myname", "jared");
    assertThat(snippet("{{ getPath() }}").render(interpreter).getValue().trim()).isEqualTo("Hello jared");
  }

  @Test
  public void testFnWithArgs() {
    TagNode t = fixture("with-args");
    assertThat(t.render(interpreter).getValue()).isEmpty();

    MacroFunction fn = (MacroFunction) interpreter.resolveObject("__macros__.section_link", -1, -1);
    assertThat(fn.getName()).isEqualTo("section_link");
    assertThat(fn.getArguments()).containsExactly("link", "text");

    assertThat(snippet("{{section_link('mylink', 'mytext')}}").render(interpreter).getValue().trim()).isEqualTo("link: mylink, text: mytext");
  }

  @Test
  public void testFnWithKwArgs() {
    TagNode t = fixture("list_kwargs");
    assertThat(t.render(interpreter).getValue()).isEmpty();

    String out = snippet("{{ list_kwargs(foo=\"bar\", bas='baz', answer=42) }}").render(interpreter).getValue().trim();
    assertThat(out).contains("foo=bar");
    assertThat(out).contains("bas=baz");
    assertThat(out).contains("answer=42");
  }

  @Test
  public void testFnWithArgsWithDefVals() {
    TagNode t = fixture("def-vals");
    assertThat(t.render(interpreter).getValue()).isEmpty();

    MacroFunction fn = (MacroFunction) interpreter.resolveObject("__macros__.article", -1, -1);
    assertThat(fn.getArguments()).containsExactly("title", "thumb", "link", "summary", "last");
    assertThat(fn.getDefaults()).contains(entry("last", false));

    assertThat(snippet("{{ article('mytitle','mythumb','mylink','mysummary') }}").render(interpreter).getValue().trim())
        .isEqualTo("title: mytitle, thumb: mythumb, link: mylink, summary: mysummary, last: false");
    assertThat(snippet("{{ article('mytitle','mythumb','mylink','mysummary', last=true) }}").render(interpreter).getValue().trim())
        .isEqualTo("title: mytitle, thumb: mythumb, link: mylink, summary: mysummary, last: true");
  }

  @Test
  public void testFnWithArrayDefVal() {
    TagNode t = fixture("array-def-val");
    assertThat(t.render(interpreter).getValue()).isEmpty();

    MacroFunction fn = (MacroFunction) interpreter.resolveObject("__macros__.prefix", -1, -1);
    assertThat(fn.getArguments()).containsExactly("property", "value", "prefixes", "prefixval");
    assertThat(fn.getDefaults()).contains(entry("prefixes", Lists.newArrayList("webkit", "moz")));
  }

  @Test
  public void testMacroUsedInForLoop() throws Exception {
    Map<String, Object> bindings = new HashMap<>();
    bindings.put("widget_data", ImmutableMap.of(
        "tools_body_1", ImmutableMap.of("html", "body1"),
        "tools_body_2", ImmutableMap.of("html", "body2"),
        "tools_body_3", ImmutableMap.of("html", "body3"),
        "tools_body_4", ImmutableMap.of("html", "body4")));

    Document dom = Jsoup.parseBodyFragment(new Jinjava().render(fixtureText("macro-used-in-forloop"), bindings));
    Element tabs = dom.select(".tabs").get(0);
    assertThat(tabs.select(".tools__description")).hasSize(4);
    assertThat(tabs.select(".tools__description").get(0).text()).isEqualTo("body1");
    assertThat(tabs.select(".tools__description").get(1).text()).isEqualTo("body2");
    assertThat(tabs.select(".tools__description").get(2).text()).isEqualTo("body3");
    assertThat(tabs.select(".tools__description").get(3).text()).isEqualTo("body4");
  }

  @Test
  public void itPreventsDirectMacroRecursion() throws IOException {
    String template = fixtureText("recursion");
    interpreter.render(template);
    assertThat(interpreter.getErrorsCopy().get(0).getMessage()).contains("Cycle detected for macro 'hello'");
  }

  @Test
  public void itPreventsIndirectMacroRecursion() throws IOException {
    String template = fixtureText("recursion_indirect");
    interpreter.render(template);
    assertThat(interpreter.getErrorsCopy().get(0).getMessage()).contains("Cycle detected for macro 'goodbye'");
  }

  @Test
  public void itAllowsMacrosCallingMacrosUsingCall() throws IOException {
    String template = fixtureText("macros-calling-macros");
    String out = interpreter.render(template);
    assertThat(interpreter.getErrorsCopy()).isEmpty();
    assertThat(out).contains("Hello World One");
    assertThat(out).contains("Hello World Two");
  }

  @Test
  public void itAllowsMacroRecursionWhenEnabledInConfiguration() throws IOException {
    // I need a different configuration here therefore
    interpreter = new Jinjava(JinjavaConfig.newBuilder().withEnableRecursiveMacroCalls(true).build()).newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);

    try {
      String template = fixtureText("ending-recursion");
      String out = interpreter.render(template);
      assertThat(interpreter.getErrorsCopy()).isEmpty();
      assertThat(out).contains("Hello Hello Hello Hello Hello");
    }
    finally {
      // and I need to cleanup my mess...
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itPreventsRecursionForMacroWithVar() {
    String jinja = "{%- macro func(var) %}" +
        "{%- for f in var %}" +
        "{{ f.val }}" +
        "{%- endfor %}" +
        "{%- endmacro %}" +
        "{%- set var = {" +
        "    'f' : {" +
        "        'val': '{{ self }}'," +
        "    }" +
        "} %}" +
        "{% set self='{{var}}' %}" +
        "{{ func(var) }}" +
        "";
    Node node = new TreeParser(interpreter, jinja).buildTree();
    assertThat(interpreter.render(node)).isEqualTo(
        "{f={val={f={val={{ self }}}}}}");
  }

  private Node snippet(String jinja) {
    return new TreeParser(interpreter, jinja).buildTree().getChildren().getFirst();
  }

  private String fixtureText(String name) {
    try {
      return Resources.toString(Resources.getResource(String.format("tags/macrotag/%s.jinja", name)), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private TagNode fixture(String name) {
    return (TagNode) snippet(fixtureText(name));
  }

}
