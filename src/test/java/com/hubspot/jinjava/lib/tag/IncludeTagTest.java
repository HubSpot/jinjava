package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;

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

}
