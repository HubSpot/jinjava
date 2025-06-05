package com.hubspot.jinjava.lib.tag;

import static com.hubspot.jinjava.loader.RelativePathResolver.CURRENT_PATH_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.eager.EagerImportTagTest.PrintPathFilter;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.tree.Node;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class ImportTagTest extends BaseInterpretingTest {

  @Before
  public void setup() {
    context.put("padding", 42);
    context.registerFilter(new PrintPathFilter());
  }

  @Test
  public void itAvoidsSimpleImportCycle() throws IOException {
    interpreter.render(
      Resources.toString(
        Resources.getResource("tags/importtag/imports-self.jinja"),
        StandardCharsets.UTF_8
      )
    );
    assertThat(context.get("c")).isEqualTo("hello");

    assertThat(interpreter.getErrorsCopy().get(0).getMessage())
      .contains("Import cycle detected", "imports-self.jinja");
  }

  @Test
  public void itAvoidsNestedImportCycle() throws IOException {
    interpreter.render(
      Resources.toString(
        Resources.getResource("tags/importtag/a-imports-b.jinja"),
        StandardCharsets.UTF_8
      )
    );
    assertThat(context.get("a")).isEqualTo("foo");
    assertThat(context.get("b")).isEqualTo("bar");

    assertThat(interpreter.getErrorsCopy().get(0).getMessage())
      .contains("Import cycle detected", "b-imports-a.jinja");
  }

  @Test
  public void itHandlesNullImportedValues() throws IOException {
    interpreter.render(
      Resources.toString(
        Resources.getResource("tags/importtag/imports-null.jinja"),
        StandardCharsets.UTF_8
      )
    );
    assertThat(context.get("foo")).isEqualTo("foo");
    assertThat(context.get("bar")).isEqualTo(null);
  }

  @Test
  public void importedContextExposesVars() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) ->
      Resources.toString(
        Resources.getResource(String.format("tags/macrotag/%s", fullName)),
        StandardCharsets.UTF_8
      )
    );
    assertThat(fixture("import"))
      .contains("wrap-padding: padding-left:42px;padding-right:42px");
  }

  // Properties from within the import are deferred too.
  // The main concern is that the key is deferred so that any
  // subsequent uses of any variables defined in the imported template are marked as deferred
  @Test
  public void itDefersImportedVariableKey() {
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property");
    assertThat(interpreter.getContext().get("pegasus")).isInstanceOf(DeferredValue.class);

    //If pegasus was deferred at the key.prop level instead of key this would resolve to a value
    assertThat(interpreter.getContext().get("expected_to_be_deferred"))
      .isInstanceOf(DeferredValue.class);
    DeferredValue deferredValue = (DeferredValue) interpreter.getContext().get("pegasus");
    Map originalValue = (Map) deferredValue.getOriginalValue();
    assertThat(originalValue.get("primary_line_height")).isNotNull();
  }

  @Test
  public void itDefersGloballyImportedVariables() {
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property-global");
    assertThat(interpreter.getContext().get("primary_line_height"))
      .isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itReconstructsDeferredImportTag() {
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    String renderedImport = fixture("import-property");
    assertThat(renderedImport)
      .contains("{% import \"tags/settag/set-var-exp.jinja\" as pegasus %}");
  }

  @Test
  public void itDoesNotRenderTagsDependingOnDeferredImport() {
    try {
      interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
      String renderedImport = fixture("import-property-global");
      assertThat(renderedImport)
        .isEqualTo(
          Resources.toString(
            Resources.getResource("tags/macrotag/import-property-global.jinja"),
            StandardCharsets.UTF_8
          )
        );
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Test
  public void itDoesNotRenderTagsDependingOnDeferredGlobalImport() {
    try {
      interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
      String renderedImport = fixture("import-property");
      assertThat(renderedImport)
        .isEqualTo(
          Resources.toString(
            Resources.getResource("tags/macrotag/import-property.jinja"),
            StandardCharsets.UTF_8
          )
        );
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Test
  public void itAddsAllDeferredNodesOfImport() {
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property");
    Set<String> deferredImages = interpreter
      .getContext()
      .getDeferredNodes()
      .stream()
      .map(Node::reconstructImage)
      .collect(Collectors.toSet());
    assertThat(
      deferredImages
        .stream()
        .filter(image -> image.contains("{% set primary_line_height"))
        .collect(Collectors.toSet())
    )
      .isNotEmpty();
  }

  @Test
  public void itAddsAllDeferredNodesOfGlobalImport() {
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property-global");
    Set<String> deferredImages = interpreter
      .getContext()
      .getDeferredNodes()
      .stream()
      .map(Node::reconstructImage)
      .collect(Collectors.toSet());
    assertThat(
      deferredImages
        .stream()
        .filter(image -> image.contains("{% set primary_line_height"))
        .collect(Collectors.toSet())
    )
      .isNotEmpty();
  }

  @Test
  public void importedContextExposesMacros() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) ->
      Resources.toString(
        Resources.getResource(String.format("tags/macrotag/%s", fullName)),
        StandardCharsets.UTF_8
      )
    );
    assertThat(fixture("import")).contains("<td height=\"42\">");
    MacroFunction fn = (MacroFunction) interpreter.resolveObject(
      "pegasus.spacer",
      -1,
      -1
    );
    assertThat(fn.getName()).isEqualTo("spacer");
    assertThat(fn.getArguments()).containsExactly("orientation", "size");
    assertThat(fn.getDefaults()).contains(entry("orientation", "h"), entry("size", 42));
  }

  @Test
  public void importedContextDoesntExposePrivateMacros() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) ->
      Resources.toString(
        Resources.getResource(String.format("tags/macrotag/%s", fullName)),
        StandardCharsets.UTF_8
      )
    );
    fixture("import");
    assertThat(context.get("_private")).isNull();
  }

  @Test
  public void importedContextFnsProperlyResolveScopedVars() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) ->
      Resources.toString(
        Resources.getResource(String.format("tags/macrotag/%s", fullName)),
        StandardCharsets.UTF_8
      )
    );
    String result = fixture("imports-macro-referencing-macro");

    assertThat(interpreter.getErrorsCopy()).isEmpty();
    assertThat(result)
      .contains("using public fn: public fn: foo")
      .contains("using private fn: private fn: bar")
      .contains("using scoped var: myscopedvar");
  }

  @Test
  public void itImportsMacroWithCall() throws IOException {
    String renderResult = interpreter.render(
      Resources.toString(
        Resources.getResource("tags/importtag/imports-macro.jinja"),
        StandardCharsets.UTF_8
      )
    );
    assertThat(renderResult.trim()).isEqualTo("");
    assertThat(interpreter.getErrorsCopy()).hasSize(0);
  }

  @Test
  public void itImportsMacroViaRelativePathWithCall() throws IOException {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private RelativePathResolver relativePathResolver = new RelativePathResolver();

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
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

    context.put(CURRENT_PATH_CONTEXT_KEY, "tags/importtag/imports-macro-relative.jinja");
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());

    String renderResult = interpreter.render(
      Resources.toString(
        Resources.getResource("tags/importtag/imports-macro-relative.jinja"),
        StandardCharsets.UTF_8
      )
    );
    assertThat(renderResult.trim()).isEqualTo("");
    assertThat(interpreter.getErrorsCopy()).hasSize(0);
  }

  @Test
  public void itSetsErrorLineNumbersCorrectly() throws IOException {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/importtag/errors/base.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(2);

    assertThat(result.getErrors().get(0).getMessage())
      .contains("Error in `tags/importtag/errors/file-with-error.jinja` on line 11");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get())
      .isEqualTo("tags/importtag/errors/file-with-error.jinja");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectlyThroughIncludeTag() throws IOException {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/importtag/errors/include.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(7);

    assertThat(result.getErrors().get(0).getMessage())
      .contains("Error in `tags/importtag/errors/file-with-error.jinja` on line 11");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get())
      .isEqualTo("tags/importtag/errors/file-with-error.jinja");
  }

  @Test
  public void itSetsErrorLineNumbersCorrectlyForImportedMacros() throws IOException {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("tags/importtag/errors/import-macro.jinja"),
        StandardCharsets.UTF_8
      ),
      new HashMap<>()
    );

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getLineno()).isEqualTo(3);

    assertThat(result.getErrors().get(0).getMessage())
      .contains("Error in `tags/importtag/errors/macro-with-error.jinja` on line 12");

    assertThat(result.getErrors().get(0).getSourceTemplate().isPresent());
    assertThat(result.getErrors().get(0).getSourceTemplate().get())
      .isEqualTo("tags/importtag/errors/macro-with-error.jinja");
  }

  @Test
  public void itCorrectlySetsNestedPaths() {
    jinjava.setResourceLocator((fullName, encoding, interpreter) ->
      Resources.toString(
        Resources.getResource(String.format("tags/macrotag/%s", fullName)),
        StandardCharsets.UTF_8
      )
    );
    context.put("foo", "foo");
    assertThat(
      interpreter.render(
        "{% import 'double-import-macro.jinja' %}{{ print_path_macro2(foo) }}"
      )
    )
      .isEqualTo("double-import-macro.jinja\n\nimport-macro.jinja\nfoo\n");
  }

  @Test
  public void itResolvesNestedRelativeImports() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "level0.jinja",
                "{% import './level1/level1.jinja' as l1 %}{{ l1.macro_level1() }}"
              );
              put(
                "level1/level1.jinja",
                "{% import './deeper/macro.jinja' as helper %}{% macro macro_level1() %}L1:{{ helper.helper_macro() }}{% endmacro %}"
              );
              put(
                "level1/deeper/macro.jinja",
                "{% macro helper_macro() %}L2:HELPER{% endmacro %}"
              );
            }
          };

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
          String template = templates.get(fullName);
          if (template == null) {
            throw new IOException("Template not found: " + fullName);
          }
          return template;
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );

    interpreter.getContext().getCurrentPathStack().push("level0.jinja", 1, 0);
    String result = interpreter.render(interpreter.getResource("level0.jinja"));

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("L1:L2:HELPER");
  }

  @Test
  public void itResolvesUpAndAcrossDirectoryPaths() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "base.jinja",
                "{% import './theme/modules/header/header.hubl.html' as header %}{{ header.render_header() }}"
              );
              put(
                "theme/modules/header/header.hubl.html",
                "{% import '../../partials/atoms/link/link.hubl.html' as link %}{% macro render_header() %}{{ link.render_link() }}{% endmacro %}"
              );
              put(
                "theme/partials/atoms/link/link.hubl.html",
                "{% macro render_link() %}LINK{% endmacro %}"
              );
            }
          };

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
          String template = templates.get(fullName);
          if (template == null) {
            throw new IOException("Template not found: " + fullName);
          }
          return template;
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );

    interpreter.getContext().getCurrentPathStack().push("base.jinja", 1, 0);
    String result = interpreter.render(interpreter.getResource("base.jinja"));

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("LINK");
  }

  @Test
  public void itResolvesProjectsAbsolutePaths() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "@projects/theme-name/modules/header.html",
                "{% import '@projects/theme-name/utils/helpers.html' as helpers %}{{ helpers.render_header() }}"
              );
              put(
                "@projects/theme-name/utils/helpers.html",
                "{% macro render_header() %}HEADER{% endmacro %}"
              );
            }
          };

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
          String template = templates.get(fullName);
          if (template == null) {
            throw new IOException("Template not found: " + fullName);
          }
          return template;
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );

    interpreter
      .getContext()
      .getCurrentPathStack()
      .push("@projects/theme-name/modules/header.html", 1, 0);
    String result = interpreter.render(
      interpreter.getResource("@projects/theme-name/modules/header.html")
    );

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("HEADER");
  }

  @Test
  public void itResolvesHubspotAbsolutePaths() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "@hubspot/common/macros.html",
                "{% import '@hubspot/common/utils.html' as utils %}{{ utils.common_macro() }}"
              );
              put(
                "@hubspot/common/utils.html",
                "{% macro common_macro() %}COMMON{% endmacro %}"
              );
            }
          };

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
          String template = templates.get(fullName);
          if (template == null) {
            throw new IOException("Template not found: " + fullName);
          }
          return template;
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );

    interpreter
      .getContext()
      .getCurrentPathStack()
      .push("@hubspot/common/macros.html", 1, 0);
    String result = interpreter.render(
      interpreter.getResource("@hubspot/common/macros.html")
    );

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("COMMON");
  }

  private String fixture(String name) {
    try {
      return interpreter.renderFlat(
        Resources.toString(
          Resources.getResource(String.format("tags/macrotag/%s.jinja", name)),
          StandardCharsets.UTF_8
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
