package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.tree.Node;

public class ImportTagTest {

  private Context context;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    Jinjava jinjava = new Jinjava();
    jinjava.setResourceLocator(new ResourceLocator() {
      @Override
      public String getString(String fullName, Charset encoding,
          JinjavaInterpreter interpreter) throws IOException {
        return Resources.toString(
            Resources.getResource(String.format("tags/macrotag/%s", fullName)), StandardCharsets.UTF_8);
      }
    });

    context = new Context();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    JinjavaInterpreter.pushCurrent(interpreter);

    context.put("padding", 42);
  }

  @After
  public void cleanup() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itAvoidsSimpleImportCycle() throws IOException {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());

    interpreter.render(Resources.toString(Resources.getResource("tags/importtag/imports-self.jinja"), StandardCharsets.UTF_8));
    assertThat(context.get("c")).isEqualTo("hello");

    assertThat(interpreter.getErrorsCopy().get(0).getMessage()).contains("Import cycle detected", "imports-self.jinja");
  }

  @Test
  public void itAvoidsNestedImportCycle() throws IOException {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());

    interpreter.render(Resources.toString(Resources.getResource("tags/importtag/a-imports-b.jinja"), StandardCharsets.UTF_8));
    assertThat(context.get("a")).isEqualTo("foo");
    assertThat(context.get("b")).isEqualTo("bar");

    assertThat(interpreter.getErrorsCopy().get(0).getMessage()).contains("Import cycle detected", "b-imports-a.jinja");
  }

  @Test
  public void importedContextExposesVars() {
    assertThat(fixture("import")).contains("wrap-padding: padding-left:42px;padding-right:42px");
  }

  @Test
  public void itDefersImportedVariable() {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property");
    assertThat(interpreter.getContext().get("pegasus")).isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itDefersGloballyImportedVariables() {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property-global");
    assertThat(interpreter.getContext().get("primary_line_height")).isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itReconstructsDeferredImportTag() {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    String renderedImport = fixture("import-property");
    assertThat(renderedImport).contains("{% import \"tags/settag/set-var-exp.jinja\" as pegasus %}");
  }

  @Test
  public void itDoesNotRenderTagsDependingOnDeferredImport() {
    try {
      Jinjava jinjava = new Jinjava();
      interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
      interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
      String renderedImport = fixture("import-property-global");
      assertThat(renderedImport).isEqualTo(Resources.toString(Resources.getResource("tags/macrotag/import-property-global.jinja"), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Test
  public void itDoesNotRenderTagsDependingOnDeferredGlobalImport() {
    try {
      Jinjava jinjava = new Jinjava();
      interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
      interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
      String renderedImport = fixture("import-property");
      assertThat(renderedImport).isEqualTo(Resources.toString(Resources.getResource("tags/macrotag/import-property.jinja"), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Test
  public void itAddsAllDeferredNodesOfImport() {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property");
    Set<String> deferredImages = interpreter.getContext().getDeferredNodes().stream().map(Node::reconstructImage).collect(Collectors.toSet());
    assertThat(deferredImages.stream().filter(image -> image.contains("{% set primary_line_height")).collect(Collectors.toSet())).isNotEmpty();
  }

  @Test
  public void itAddsAllDeferredNodesOfGlobalImport() {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    interpreter.getContext().put("primary_font_size_num", DeferredValue.instance());
    fixture("import-property-global");
    Set<String> deferredImages = interpreter.getContext().getDeferredNodes().stream().map(Node::reconstructImage).collect(Collectors.toSet());
    assertThat(deferredImages.stream().filter(image -> image.contains("{% set primary_line_height")).collect(Collectors.toSet())).isNotEmpty();
  }

  @Test
  public void importedContextExposesMacros() {
    assertThat(fixture("import")).contains("<td height=\"42\">");
    MacroFunction fn = (MacroFunction) interpreter.resolveObject("pegasus.spacer", -1, -1);
    assertThat(fn.getName()).isEqualTo("spacer");
    assertThat(fn.getArguments()).containsExactly("orientation", "size");
    assertThat(fn.getDefaults()).contains(entry("orientation", "h"), entry("size", 42));
  }

  @Test
  public void importedContextDoesntExposePrivateMacros() {
    fixture("import");
    assertThat(context.get("_private")).isNull();
  }

  @Test
  public void importedContextFnsProperlyResolveScopedVars() {
    String result = fixture("imports-macro-referencing-macro");

    assertThat(interpreter.getErrorsCopy()).isEmpty();
    assertThat(result)
        .contains("using public fn: public fn: foo")
        .contains("using private fn: private fn: bar")
        .contains("using scoped var: myscopedvar");
  }

  @Test
  public void itImportsMacroWithCall() throws IOException {
    Jinjava jinjava = new Jinjava();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());

    String renderResult = interpreter.render(Resources.toString(Resources.getResource("tags/importtag/imports-macro.jinja"), StandardCharsets.UTF_8));
    assertThat(renderResult.trim()).isEqualTo("");
    assertThat(interpreter.getErrorsCopy()).hasSize(0);
  }

  private String fixture(String name) {
    try {
      return interpreter.renderFlat(Resources.toString(
          Resources.getResource(String.format("tags/macrotag/%s.jinja", name)), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
