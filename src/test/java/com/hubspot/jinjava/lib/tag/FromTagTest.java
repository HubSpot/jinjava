package com.hubspot.jinjava.lib.tag;

import static com.hubspot.jinjava.loader.RelativePathResolver.CURRENT_PATH_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class FromTagTest extends BaseInterpretingTest {

  @Before
  public void setup() {
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
            Resources.getResource(String.format("tags/macrotag/%s", fullName)),
            StandardCharsets.UTF_8
          );
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );
    context.put("padding", 42);
  }

  @Test
  public void importedContextExposesVars() {
    assertThat(fixture("from"))
      .contains("wrap-spacer:")
      .contains("<td height=\"42\">")
      .contains("wrap-padding: padding-left:42px;padding-right:42px");
  }

  @Test
  public void itImportsAliasedMacroName() {
    assertThat(fixture("from-alias-macro"))
      .contains("wrap-spacer:")
      .contains("<td height=\"42\">")
      .contains("wrap-padding: padding-left:42px;padding-right:42px");
  }

  @Test
  public void importedCycleDected() {
    fixture("from-recursion");
    assertTrue(
      interpreter
        .getErrorsCopy()
        .stream()
        .anyMatch(e -> e.getCategory() == BasicTemplateErrorCategory.FROM_CYCLE_DETECTED)
    );
  }

  @Test
  public void importedIndirectCycleDected() {
    fixture("from-a-to-b");
    assertTrue(
      interpreter
        .getErrorsCopy()
        .stream()
        .anyMatch(e -> e.getCategory() == BasicTemplateErrorCategory.FROM_CYCLE_DETECTED)
    );
  }

  @Test
  public void itImportsWithMacroTag() {
    fixture("from-simple-with-call");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itImportsViaRelativePath() {
    interpreter
      .getContext()
      .put(CURRENT_PATH_CONTEXT_KEY, "relative/relative-from.jinja");
    fixture("relative-from");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itDefersImport() {
    interpreter.getContext().put("padding", DeferredValue.instance());
    String template = fixtureText("from");
    String rendered = fixture("from");
    assertThat(rendered).isEqualTo(template);
    MacroFunction spacer = interpreter.getContext().getGlobalMacro("spacer");
    assertThat(spacer.isDeferred()).isTrue();
  }

  private String fixture(String name) {
    return interpreter.renderFlat(fixtureText(name));
  }

  private String fixtureText(String name) {
    try {
      return Resources.toString(
        Resources.getResource(String.format("tags/macrotag/%s.jinja", name)),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
