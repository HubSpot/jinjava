package com.hubspot.jinjava.lib.tag;

import static com.hubspot.jinjava.loader.RelativePathResolver.CURRENT_PATH_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;

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
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
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
  public void importedCycleDetected() {
    fixture("from-recursion");
    assertThat(
      interpreter
        .getErrorsCopy()
        .stream()
        .anyMatch(e -> e.getCategory() == BasicTemplateErrorCategory.FROM_CYCLE_DETECTED)
    )
      .isTrue();
  }

  @Test
  public void importedIndirectCycleDetected() {
    fixture("from-a-to-b");
    assertThat(
      interpreter
        .getErrorsCopy()
        .stream()
        .anyMatch(e -> e.getCategory() == BasicTemplateErrorCategory.FROM_CYCLE_DETECTED)
    )
      .isTrue();
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
                "{% from 'level1/nested.jinja' import macro1 %}{{ macro1() }}"
              );
              put(
                "level1/nested.jinja",
                "{% from '../level1/deeper/macro.jinja' import macro2 %}{% macro macro1() %}L1:{{ macro2() }}{% endmacro %}"
              );
              put(
                "level1/deeper/macro.jinja",
                "{% from '../../utils/helper.jinja' import helper %}{% macro macro2() %}L2:{{ helper() }}{% endmacro %}"
              );
              put("utils/helper.jinja", "{% macro helper() %}HELPER{% endmacro %}");
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
  public void itMaintainsPathStackIntegrity() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "root.jinja",
                "{% from 'simple/macro.jinja' import simple_macro %}{{ simple_macro() }}"
              );
              put("simple/macro.jinja", "{% macro simple_macro() %}SIMPLE{% endmacro %}");
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

    interpreter.getContext().getCurrentPathStack().push("root.jinja", 1, 0);
    Optional<String> initialTopPath = interpreter
      .getContext()
      .getCurrentPathStack()
      .peek();

    interpreter.render(interpreter.getResource("root.jinja"));

    assertThat(interpreter.getContext().getCurrentPathStack().peek())
      .isEqualTo(initialTopPath);
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itWorksWithIncludeAndFromTogether() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "mixed-tags.jinja",
                "{% from 'macros/test.jinja' import test_macro %}{% include 'includes/content.jinja' %}{{ test_macro() }}"
              );
              put(
                "macros/test.jinja",
                "{% from '../utils/shared.jinja' import shared %}{% macro test_macro() %}MACRO:{{ shared() }}{% endmacro %}"
              );
              put(
                "includes/content.jinja",
                "{% from '../utils/shared.jinja' import shared %}INCLUDE:{{ shared() }}"
              );
              put("utils/shared.jinja", "{% macro shared() %}SHARED{% endmacro %}");
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

    interpreter.getContext().getCurrentPathStack().push("mixed-tags.jinja", 1, 0);
    String result = interpreter.render(interpreter.getResource("mixed-tags.jinja"));

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).contains("INCLUDE:SHARED");
    assertThat(result.trim()).contains("MACRO:SHARED");
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
                "theme/hubl-modules/navigation.module/module.hubl.html",
                "{% from '../../partials/atoms/link/link.hubl.html' import link_macro %}{{ link_macro() }}"
              );
              put(
                "theme/partials/atoms/link/link.hubl.html",
                "{% from '../icons/icons.hubl.html' import icon_macro %}{% macro link_macro() %}LINK:{{ icon_macro() }}{% endmacro %}"
              );
              put(
                "theme/partials/atoms/icons/icons.hubl.html",
                "{% macro icon_macro() %}ICON{% endmacro %}"
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
      .push("theme/hubl-modules/navigation.module/module.hubl.html", 1, 0);
    String result = interpreter.render(
      interpreter.getResource("theme/hubl-modules/navigation.module/module.hubl.html")
    );

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("LINK:ICON");
  }

  @Test
  public void itResolvesProjectsAbsolutePathsWithNestedRelativeImports()
    throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "@projects/theme-a/modules/header/header.html",
                "{% from '../../components/button.html' import render_button %}{{ render_button('primary') }}"
              );
              put(
                "@projects/theme-a/components/button.html",
                "{% from '../utils/icons.html' import get_icon %}{% macro render_button(type) %}{{ type }}-{{ get_icon() }}{% endmacro %}"
              );
              put(
                "@projects/theme-a/utils/icons.html",
                "{% macro get_icon() %}ICON{% endmacro %}"
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
      .push("@projects/theme-a/modules/header/header.html", 1, 0);
    String result = interpreter.render(
      interpreter.getResource("@projects/theme-a/modules/header/header.html")
    );

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("primary-ICON");
  }

  @Test
  public void itResolvesHubspotAbsolutePathsWithNestedRelativeImports() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "@hubspot/modules/forms/contact-form.html",
                "{% from '../../shared/validation.html' import validate_field %}{{ validate_field('email') }}"
              );
              put(
                "@hubspot/shared/validation.html",
                "{% from '../helpers/formatters.html' import format_error %}{% macro validate_field(field) %}{{ format_error(field) }}{% endmacro %}"
              );
              put(
                "@hubspot/helpers/formatters.html",
                "{% macro format_error(field) %}ERROR:{{ field }}{% endmacro %}"
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
      .push("@hubspot/modules/forms/contact-form.html", 1, 0);
    String result = interpreter.render(
      interpreter.getResource("@hubspot/modules/forms/contact-form.html")
    );

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("ERROR:email");
  }

  @Test
  public void itResolvesMixedAbsoluteAndRelativeImports() throws Exception {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private final RelativePathResolver relativePathResolver =
          new RelativePathResolver();
        private final java.util.Map<String, String> templates =
          new java.util.HashMap<>() {
            {
              put(
                "@projects/mixed/module.html",
                "{% from '@hubspot/shared/globals.html' import global_helper %}{{ global_helper() }}"
              );
              put(
                "@hubspot/shared/globals.html",
                "{% from '../utils/common.html' import format_text %}{% macro global_helper() %}{{ format_text('MIXED') }}{% endmacro %}"
              );
              put(
                "@hubspot/utils/common.html",
                "{% macro format_text(text) %}FORMAT:{{ text }}{% endmacro %}"
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
      .push("@projects/mixed/module.html", 1, 0);
    String result = interpreter.render(
      interpreter.getResource("@projects/mixed/module.html")
    );

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result.trim()).isEqualTo("FORMAT:MIXED");
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
