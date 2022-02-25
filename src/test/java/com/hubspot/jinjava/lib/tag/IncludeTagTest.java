package com.hubspot.jinjava.lib.tag;

import static com.hubspot.jinjava.loader.RelativePathResolver.CURRENT_PATH_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Splitter;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import org.junit.Test;

public class IncludeTagTest extends BaseInterpretingTest {

  @Test
  public void itAvoidsSimpleIncludeCycles() throws IOException {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("tags/includetag/includes-self.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<String, Object>()
    );
    assertThat(result).containsSequence("hello world", "hello world");
  }

  @Test
  public void itAvoidsNestedIncludeCycles() throws IOException {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("tags/includetag/a-includes-b.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<String, Object>()
    );
    assertThat(result).containsSequence("A", "B");
  }

  @Test
  public void itAllowsSameIncludeMultipleTimesInATemplate() throws IOException {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("tags/includetag/c-includes-d-twice.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<String, Object>()
    );
    assertThat(Splitter.on('\n').omitEmptyStrings().trimResults().split(result))
      .containsExactly("hello", "hello");
  }

  @Test
  public void itHasIncludesReferenceInContext() throws Exception {
    RenderResult renderResult = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/includetag/include-tag-dependencies.html"),
        StandardCharsets.UTF_8
      ),
      new HashMap<String, Object>()
    );

    SetMultimap<String, String> dependencies = renderResult
      .getContext()
      .getDependencies();

    assertThat(dependencies.size()).isEqualTo(2);
    assertThat(dependencies.get("coded_files")).isNotEmpty();

    assertThat(
      dependencies
        .get("coded_files")
        .contains("{% include \"tags/includetag/hello.html\" %}")
    );
    assertThat(
      dependencies
        .get("coded_files")
        .contains("{% include \"tags/includetag/cat.html\" %}")
    );
  }

  @Test
  public void itIncludesFileWithMacroCall() throws IOException {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/includetag/include-with-import.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  public void itIncludesFileWithInternalMacroCall() throws IOException {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/macrotag/include-two-macros.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  public void itIncludesFileViaRelativePath() throws IOException {
    jinjava = new Jinjava();
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private RelativePathResolver relativePathResolver = new RelativePathResolver();

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        )
          throws IOException {
          return Resources.toString(
            Resources.getResource(String.format("%s", fullName)),
            StandardCharsets.UTF_8
          );
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );

    jinjava
      .getGlobalContext()
      .put(CURRENT_PATH_CONTEXT_KEY, "tags/includetag/includes-relative-path.jinja");
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/includetag/includes-relative-path.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getOutput().trim()).isEqualTo("INCLUDED");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectly() throws IOException {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/includetag/errors/base.html"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(7);

    assertThat(result.getErrors().get(0).getMessage())
      .contains("Error in `tags/includetag/errors/error.html` on line 4");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get())
      .isEqualTo("tags/includetag/errors/error.html");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectlyTwoLevelsDeep() throws IOException {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/includetag/errors/base2.html"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(2);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("Error in `tags/includetag/errors/error.html` on line 4");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get())
      .isEqualTo("tags/includetag/errors/error.html");
  }

  @Test
  public void itAvoidsTagCycleExceptionInsideExtendedFiles() throws Exception {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("tags/extendstag/tagcycleexception/template-a.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );
    assertThat(result).isEqualTo("Extended text, will be rendered");
  }

  @Test
  public void itIgnoresMissing() throws IOException {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("tags/includetag/missing-include.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<String, Object>()
    );
    assertThat(result).containsSequence("AB\nCD");
  }
}
