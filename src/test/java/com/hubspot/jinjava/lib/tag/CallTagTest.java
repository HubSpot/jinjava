package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class CallTagTest {

  JinjavaInterpreter interpreter;
  Context context;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    context = interpreter.getContext();
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void cleanup() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void testSimpleFn() {
    Document dom = Jsoup.parseBodyFragment(interpreter.render(fixture("simple")));
    assertThat(dom.select("div h2").text().trim()).isEqualTo("Hello World");
    assertThat(dom.select("div.contents").text().trim()).isEqualTo("This is a simple dialog rendered by using a macro and a call block.");
  }

  @Test
  public void testArgsFn() {
    List<Map<String, String>> users = new ArrayList<Map<String, String>>();
    Map<String, String> u = new HashMap<String, String>();
    u.put("username", "jdoe");
    u.put("realname", "John Doe");
    u.put("description", "Test user");
    users.add(u);
    u = new HashMap<String, String>();
    u.put("username", "root");
    u.put("realname", "God");
    u.put("description", "Superuser");
    users.add(u);
    context.put("list_of_user", users);
    Document dom = Jsoup.parseBodyFragment(interpreter.render(fixture("args")));
    assertThat(dom.select("ul p").text().trim()).isEqualTo("jdoe root");
    assertThat(dom.select("li:first-child dl > dl:first-child + dd").text().trim()).isEqualTo("John Doe");
    assertThat(dom.select("li:first-child dl > dl:first-child + dd + dl + dd").text().trim()).isEqualTo("Test user");
    assertThat(dom.select("li:first-child + li dl > dl:first-child + dd").text().trim()).isEqualTo("God");
    assertThat(dom.select("li:first-child + li dl > dl:first-child + dd + dl + dd").text().trim()).isEqualTo("Superuser");
  }

  @Test
  public void testMultTable() {
    context.put("list_of_numbers", IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList()));
    Document dom = Jsoup.parseBodyFragment(interpreter.render(fixture("mult_table")));
    assertThat(dom.select("#3x3").text().trim()).isEqualTo("9");
    assertThat(dom.select("#9x3").text().trim()).isEqualTo("27");
    assertThat(dom.select("#3x9").text().trim()).isEqualTo("27");
  }

  private String fixture(String name) {
    try {
      return Resources.toString(Resources.getResource(String.format("tags/calltag/%s.jinja", name)), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
