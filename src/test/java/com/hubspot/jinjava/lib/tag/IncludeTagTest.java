package com.hubspot.jinjava.lib.tag;

import static com.hubspot.jinjava.loader.RelativePathResolver.CURRENT_PATH_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;

public class IncludeTagTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itAvoidsSimpleIncludeCycles() throws IOException {
    String result = jinjava.render(Resources.toString(Resources.getResource("tags/includetag/includes-self.jinja"), StandardCharsets.UTF_8),
        new HashMap<String, Object>());
    assertThat(result).containsSequence("hello world", "hello world");
  }

  @Test
  public void itAvoidsNestedIncludeCycles() throws IOException {
    String result = jinjava.render(Resources.toString(Resources.getResource("tags/includetag/a-includes-b.jinja"), StandardCharsets.UTF_8),
        new HashMap<String, Object>());
    assertThat(result).containsSequence("A", "B");
  }

  @Test
  public void itAllowsSameIncludeMultipleTimesInATemplate() throws IOException {
    String result = jinjava.render(Resources.toString(Resources.getResource("tags/includetag/c-includes-d-twice.jinja"), StandardCharsets.UTF_8),
        new HashMap<String, Object>());
    assertThat(Splitter.on('\n').omitEmptyStrings().trimResults().split(result)).containsExactly("hello", "hello");
  }

  @Test
  public void itHasIncludesReferenceInContext() throws Exception {
    RenderResult renderResult = jinjava.renderForResult(Resources.toString(Resources.getResource("tags/includetag/include-tag-dependencies.html"), StandardCharsets.UTF_8),
        new HashMap<String, Object>());

    SetMultimap<String, String> dependencies = renderResult.getContext().getDependencies();

    assertThat(dependencies.size()).isEqualTo(2);
    assertThat(dependencies.get("coded_files")).isNotEmpty();

    assertThat(dependencies.get("coded_files").contains("{% include \"tags/includetag/hello.html\" %}"));
    assertThat(dependencies.get("coded_files").contains("{% include \"tags/includetag/cat.html\" %}"));
  }

  @Test
  public void itIncludesFileWithMacroCall() throws IOException {

    RenderResult result = jinjava.renderForResult(Resources.toString(Resources.getResource("tags/includetag/include-with-import.jinja"), StandardCharsets.UTF_8),
        new HashMap<>());

    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  public void itIncludesFileViaRelativePath() throws IOException {
    jinjava = new Jinjava();
    jinjava.setResourceLocator(new ResourceLocator() {
      private RelativePathResolver relativePathResolver = new RelativePathResolver();

      @Override
      public String getString(String fullName, Charset encoding,
                              JinjavaInterpreter interpreter) throws IOException {
        return Resources.toString(
            Resources.getResource(String.format("%s", fullName)), StandardCharsets.UTF_8);
      }

      @Override
      public Optional<LocationResolver> getLocationResolver() {
        return Optional.of(relativePathResolver);
      }
    });

    jinjava.getGlobalContext().put(CURRENT_PATH_CONTEXT_KEY, "tags/includetag/includes-relative-path.jinja");
    RenderResult result = jinjava.renderForResult(Resources.toString(Resources.getResource("tags/includetag/includes-relative-path.jinja"), StandardCharsets.UTF_8),
        new HashMap<>());

    assertThat(result.getOutput().trim()).isEqualTo("INCLUDED");
  }

}
