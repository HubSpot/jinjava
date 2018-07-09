package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;

public class ForTagTest {

  ForTag tag;

  private Context context;
  JinjavaInterpreter interpreter;
  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
    context = interpreter.getContext();

    tag = new ForTag();
  }

  @Test
  public void forLoopUsingLoopLastVar() {
    context.put("the_list", Lists.newArrayList(1L, 2L, 3L, 7L));
    TagNode tagNode = (TagNode) fixture("loop-last-var");
    Document dom = Jsoup.parseBodyFragment(tag.interpret(tagNode, interpreter));

    assertThat(dom.select("h3")).hasSize(3);
  }

  @Test
  public void forLoopUsingScalarValue() {
    context.put("the_list", 999L);
    TagNode tagNode = (TagNode) fixture("loop-with-scalar");
    String output = tag.interpret(tagNode, interpreter);
    assertThat(output.trim()).isEqualTo("<h3>The Number: 999</h3>");
  }

  @Test
  public void forLoopNestedFor() {
    TagNode tagNode = (TagNode) fixture("nested-fors");
    assertThat(Splitter.on("\n").trimResults().omitEmptyStrings().split(
        tag.interpret(tagNode, interpreter)))
            .contains("02", "03", "12", "13");
  }

  @Test
  public void forLoopMultipleLoopVars() {
    Map<String, Object> dict = Maps.newHashMap();
    dict.put("foo", "one");
    dict.put("bar", 2L);

    context.put("the_dictionary", dict);
    TagNode tagNode = (TagNode) fixture("multiple-loop-vars");
    Document dom = Jsoup.parseBodyFragment(tag.interpret(tagNode, interpreter));

    assertThat(dom.select("p")).hasSize(2);
  }

  @Test
  public void forLoopMultipleLoopVarsArbitraryNames() {
    Map<String, Object> dict = ImmutableMap.of(
        "grand", "ol'",
        "adserving", "team");

    context.put("the_dictionary", dict);
    String template = ""
        + "{% for foo, bar in the_dictionary.items() %}"
        + "{{ foo }}: {{ bar }}\n"
        + "{% endfor %}";

    String rendered = jinjava.render(template, context);
    assertEquals("grand: ol'\nadserving: team\n", rendered);
  }

  @Test
  public void forLoopLiteralLoopExpr() {
    TagNode tagNode = (TagNode) fixture("literal-loop-expr");
    assertThat(tag.interpret(tagNode, interpreter)).isEqualTo("012345");
  }

  @Test
  public void forLoopWithNestedCycle() {
    context.put("cycle1", "odd");
    context.put("cycle2", "even");

    TagNode tagNode = (TagNode) fixture("nested-cycle");
    List<String> result = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(tag.interpret(tagNode, interpreter)));
    assertThat(result).containsExactly("odd", "even", "odd", "even", "odd");
  }

  @Test
  public void forLoopIndexVar() {
    TagNode tagNode = (TagNode) fixture("loop-index-var");
    assertThat(tag.interpret(tagNode, interpreter)).isEqualTo("012345");
  }

  @Test
  public void forLoopSupportsAllLoopVarsInHublDocs() {
    TagNode tagNode = (TagNode) fixture("hubl-docs-loop-vars");
    Document dom = Jsoup.parseBodyFragment(tag.interpret(tagNode, interpreter));

    Elements els = dom.select(".item");
    assertThat(els).hasSize(4);

    assertThat(dom.select(".item-0 .index").text()).isEqualTo("1");
    assertThat(dom.select(".item-0 .index0").text()).isEqualTo("0");
    assertThat(dom.select(".item-0 .first").text()).isEqualTo("true");
    assertThat(dom.select(".item-0 .last").text()).isEqualTo("false");
    assertThat(dom.select(".item-0 .revindex").text()).isEqualTo("4");
    assertThat(dom.select(".item-0 .revindex0").text()).isEqualTo("3");
    assertThat(dom.select(".item-0 .length").text()).isEqualTo("4");
    assertThat(dom.select(".item-0 .depth").text()).isEqualTo("1");
    assertThat(dom.select(".item-0 .depth0").text()).isEqualTo("0");

    assertThat(dom.select(".item-0 .subnum").text()).isEqualTo("6 0");
  }

  @Test
  public void testForLoopConstants() {

      Map<String, Object> context = Maps.newHashMap();
      String template = ""
              + "{% for i in range(1 * 1, 2 * 2) %}{{i}}{% endfor %}";

      String rendered = jinjava.render(template, context);
      assertEquals("123", rendered);
  }

  @Test
  public void testForLoopVariablesWithoutSpaces() {

      Map<String, Object> context = Maps.newHashMap();
      context.put("a", 2);
      context.put("b", 3);

      String template = ""
          + "{% for index in range(a*b,a*b+b) %}"
          + "{{index}} "
          + "{% endfor %}";

      String rendered = jinjava.render(template, context);
      assertEquals("6 7 8 ", rendered);
  }

  @Test
  public void testForLoopVariablesWithSpaces() {

      Map<String, Object> context = Maps.newHashMap();
      context.put("a", 2);
      context.put("b", 3);

      String template = ""
          + "{% for index in range(a * b, a * b + b) %}"
          + "{{index}} "
          + "{% endfor %}";

      String rendered = jinjava.render(template, context);
      assertEquals("6 7 8 ", rendered);
  }

  @Test
  public void testForLoopRangeWithStringsWithSpaces() {
      Map<String, Object> context = Maps.newHashMap();
      String template = ""
           + "{% for i in ['a ','b'] %}{{i}}{% endfor %}";
      String rendered = jinjava.render(template, context);
      System.out.println(rendered);
      assertEquals("a b", rendered);
  }

  @Test
  public void testForLoopWithDates() {
    Map<String, Object> context = Maps.newHashMap();
    Date testDate = new Date();
    context.put("the_list", Lists.newArrayList(testDate));
    String template = ""
        + "{% for i in the_list %}{{i}}{% endfor %}";
    String rendered = jinjava.render(template, context);
    System.out.println(rendered);
    assertEquals(new PyishDate(testDate).toString(), rendered);
  }

  private Node fixture(String name) {
    try {
      return new TreeParser(interpreter, Resources.toString(
          Resources.getResource(String.format("tags/fortag/%s.jinja", name)), StandardCharsets.UTF_8))
              .buildTree().getChildren().getFirst();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
