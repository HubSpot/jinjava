package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.ResourceLocator;



public class ExtendsTagTest {

  private ExtendsTagTestResourceLocator locator;
  private Jinjava application;

  @Before
  public void setup() {
    locator = new ExtendsTagTestResourceLocator();
    
    JinjavaConfig conf = new JinjavaConfig();
    application = new Jinjava(conf);
    application.setResourceLocator(locator);
  }
  
  @Test
  public void singleExtendsTag() throws Exception {
    String result = application.render(locator.fixture("extends-base1.jinja"), new HashMap<String, Object>());
    Document dom = Jsoup.parse(result);
    assertThat(dom.select(".important").get(0).text()).isEqualTo("Welcome on my awesome homepage.");
  }
  
  @Test
  public void nestedExtendsHierarchy() throws Exception {
    String result = application.render(locator.fixture("extends-base2.jinja"), new HashMap<String, Object>());
    Document dom = Jsoup.parse(result);
    assertThat(dom.select(".important").get(0).text()).isEqualTo("foobar");
  }
  
  @Test
  public void parentTemplateImportsVarsUsedInChild() throws Exception {
    Document dom = Jsoup.parse(application.render(locator.fixture("parentvars-child.html"), new HashMap<String, Object>()));

    assertThat(dom.select("body").attr("bgcolor")).isEqualTo("#f5f5f5");
    assertThat(dom.select("p.foo").text()).isEqualTo("bar");
  }
  
  @Test
  public void extendsWithSuperCall() throws Exception {
    Document dom = Jsoup.parse(application.render(locator.fixture("super-child.html"), new HashMap<String, Object>()));

    assertThat(dom.select(".sidebar p").text()).isEqualTo("this is a sidebar.");
    assertThat(dom.select(".sidebar h3").text()).isEqualTo("Table Of Contents");
  }
  
  private static class ExtendsTagTestResourceLocator implements ResourceLocator {
    @Override
    public String getString(String fullName, Charset encoding, JinjavaInterpreter interpreter) throws IOException {
      return fixture(fullName);
    }
    public String fixture(String name) throws IOException {
      return Resources.toString(Resources.getResource(String.format("tags/extendstag/%s", name)), Charsets.UTF_8);
    }
  }
}
