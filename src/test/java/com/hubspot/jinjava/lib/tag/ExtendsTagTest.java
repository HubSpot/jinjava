package com.hubspot.jinjava.lib.tag;

import static com.hubspot.jinjava.loader.RelativePathResolver.CURRENT_PATH_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.SetMultimap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TagCycleException;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;

public class ExtendsTagTest {

  private ExtendsTagTestResourceLocator locator;
  private Jinjava jinjava;

  @Before
  public void setup() {
    locator = new ExtendsTagTestResourceLocator();

    JinjavaConfig conf = new JinjavaConfig();
    jinjava = new Jinjava(conf);
    jinjava.setResourceLocator(locator);
  }

  @Test
  public void singleExtendsTag() throws Exception {
    String result = jinjava.render(locator.fixture("extends-base1.jinja"), new HashMap<String, Object>());
    Document dom = Jsoup.parse(result);
    assertThat(dom.select(".important").get(0).text()).isEqualTo("Welcome on my awesome homepage.");
  }

  @Test
  public void nestedExtendsHierarchy() throws Exception {
    String result = jinjava.render(locator.fixture("extends-base2.jinja"), new HashMap<String, Object>());
    Document dom = Jsoup.parse(result);
    assertThat(dom.select(".important").get(0).text()).isEqualTo("foobar");
  }

  @Test
  public void parentTemplateImportsVarsUsedInChild() throws Exception {
    Document dom = Jsoup.parse(jinjava.render(locator.fixture("parentvars-child.html"), new HashMap<String, Object>()));

    assertThat(dom.select("body").attr("bgcolor")).isEqualTo("#f5f5f5");
    assertThat(dom.select("p.foo").text()).isEqualTo("bar");
  }

  @Test
  public void extendsWithSuperCall() throws Exception {
    Document dom = Jsoup.parse(jinjava.render(locator.fixture("super-child.html"), new HashMap<String, Object>()));

    assertThat(dom.select(".sidebar p").text()).isEqualTo("this is a sidebar.");
    assertThat(dom.select(".sidebar h3").text()).isEqualTo("Table Of Contents");
  }

  @Test
  public void itHasExtendsReferenceInContext() throws Exception {
    RenderResult renderResult = jinjava.renderForResult(locator.fixture("super-child.html"), new HashMap<String, Object>());
    SetMultimap<String, String> dependencies = renderResult.getContext().getDependencies();

    assertThat(dependencies.size()).isEqualTo(1);
    assertThat(dependencies.get("coded_files")).isNotEmpty();

    assertThat(dependencies.get("coded_files").contains("super-base.html"));
  }

  @Test
  public void itAvoidsSimpleExtendsCycles() throws IOException {
    try {
      jinjava.render(locator.fixture("extends-self.jinja"),
          new HashMap<String, Object>());
      fail("expected extends tag cycle exception");
    } catch (FatalTemplateErrorsException e) {
      TagCycleException cycleException = (TagCycleException) e.getErrors().iterator().next().getException();
      assertThat(cycleException.getPath()).isEqualTo("extends-self.jinja");
    }
  }

  @Test
  public void itAvoidsNestedExtendsCycles() throws IOException {
    try {
      jinjava.render(locator.fixture("a-extends-b.jinja"),
          new HashMap<String, Object>());
      fail("expected extends tag cycle exception");
    } catch (FatalTemplateErrorsException e) {
      TagCycleException cycleException = (TagCycleException) e.getErrors().iterator().next().getException();
      assertThat(cycleException.getPath()).isEqualTo("b-extends-a.jinja");
    }
  }

  @Test
  public void itExtendsViaRelativePath() throws IOException {
    jinjava.getGlobalContext().put(CURRENT_PATH_CONTEXT_KEY, "relative/relative-extends.jinja");
    String result = jinjava.render(locator.fixture("relative/relative-extends.jinja"), new HashMap<>());
    assertThat(result).contains("This is a relative path extends");
  }

  @Test
  public void itHandlesRelativePathsInBlocksInExtendingTemplate() throws IOException {
    jinjava.getGlobalContext().put(CURRENT_PATH_CONTEXT_KEY, "relative/relative-block.jinja");
    String result = jinjava.render(locator.fixture("relative/relative-block.jinja"), new HashMap<>());
    assertThat(result).contains("hello");
  }

  @Test
  public void itHandlesRelativePathsInBlocksFromExtendedTemplate() throws IOException {
    jinjava.getGlobalContext().put(CURRENT_PATH_CONTEXT_KEY, "relative/relative-block-2.jinja");
    String result = jinjava.render(locator.fixture("relative/relative-block-2.jinja"), new HashMap<>());
    assertThat(result).contains("hello");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectlyInBlocksInExtendingTemplate() throws IOException {
    RenderResult result = jinjava.renderForResult(locator.fixture("errors/extends-no-error.jinja"), new HashMap<>());

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(8);
    assertThat(result.getErrors().get(0).getMessage()).doesNotContain("Error in `/errors/no-error.html` on line");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectlyInBlocksFromExtendedTemplate() throws IOException {
    RenderResult result = jinjava.renderForResult(locator.fixture("errors/extends-block-error.jinja"), new HashMap<>());

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(5);
    assertThat(result.getErrors().get(0).getMessage()).contains("Error in `/errors/block-error.html` on line 16");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get()).isEqualTo("/errors/block-error.html");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectlyOutsideBlocksFromExtendedTemplate() throws IOException {
    RenderResult result = jinjava.renderForResult(locator.fixture("errors/extends-outside-block-error.jinja"), new HashMap<>());

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(1);
    assertThat(result.getErrors().get(0).getMessage()).contains("Error in `/errors/outside-block-error.html` on line 9");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get()).isEqualTo("/errors/outside-block-error.html");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectlyInBlocksFromExtendedTemplateInIncludedTemplate() throws IOException {
    RenderResult result = jinjava.renderForResult(locator.fixture("errors/extends-include-block-error.jinja"), new HashMap<>());

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(3);
    assertThat(result.getErrors().get(0).getMessage()).contains("Error in `/errors/error.html` on line 5");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get()).isEqualTo("/errors/error.html");
  }

  private static class ExtendsTagTestResourceLocator implements ResourceLocator {
    private RelativePathResolver relativePathResolver = new RelativePathResolver();

    @Override
    public String getString(String fullName, Charset encoding, JinjavaInterpreter interpreter) throws IOException {
      return fixture(fullName);
    }

    public String fixture(String name) throws IOException {
      return Resources.toString(Resources.getResource(String.format("tags/extendstag/%s", name)), StandardCharsets.UTF_8);
    }

    @Override
    public Optional<LocationResolver> getLocationResolver() {
      return Optional.of(relativePathResolver);
    }
  }
}
