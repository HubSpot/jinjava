package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.ExpectedNodeInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.FromTag;
import com.hubspot.jinjava.lib.tag.ImportTagTest;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyMap;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EagerImportTagTest extends ImportTagTest {
  private static final String CONTEXT_VAR = "context_var";
  private static final String TEMPLATE_FILE = "template.jinja";
  private ExpectedNodeInterpreter expectedNodeInterpreter;

  @Before
  public void eagerSetup() {
    context.put("padding", 42);
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig.newBuilder().withExecutionMode(new EagerExecutionMode()).build()
      );
    Tag tag = EagerTagFactory
      .getEagerTagDecorator(FromTag.class)
      .orElseThrow(RuntimeException::new);
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    expectedNodeInterpreter =
      new ExpectedNodeInterpreter(interpreter, tag, "tags/eager/importtag");
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itRemovesKeysFromChildBindings() {
    JinjavaInterpreter child = getChildInterpreter(interpreter, CONTEXT_VAR);
    Map<String, Object> childBindings = child.getContext().getSessionBindings();
    assertThat(childBindings.get(Context.IMPORT_RESOURCE_ALIAS_KEY))
      .isEqualTo(CONTEXT_VAR);
    EagerImportTag.integrateChild(CONTEXT_VAR, childBindings, child, interpreter);
    assertThat(interpreter.getContext().get(CONTEXT_VAR)).isInstanceOf(Map.class);
    assertThat(((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).keySet())
      .doesNotContain(Context.IMPORT_RESOURCE_ALIAS_KEY);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itHandlesMultiLayer() {
    JinjavaInterpreter child = getChildInterpreter(interpreter, "");
    JinjavaInterpreter child2 = getChildInterpreter(child, "");
    child2.getContext().put("foo", "foo val");
    child.getContext().put("bar", "bar val");
    EagerImportTag.integrateChild(
      "",
      child2.getContext().getSessionBindings(),
      child2,
      child
    );
    EagerImportTag.integrateChild(
      "",
      child.getContext().getSessionBindings(),
      child,
      interpreter
    );
    assertThat(interpreter.getContext().get("foo")).isEqualTo("foo val");
    assertThat(interpreter.getContext().get("bar")).isEqualTo("bar val");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itHandlesMultiLayerAliased() {
    String child2Alias = "double_child";
    JinjavaInterpreter child = getChildInterpreter(interpreter, CONTEXT_VAR);
    JinjavaInterpreter child2 = getChildInterpreter(child, child2Alias);

    child2.render("{% set foo = 'foo val' %}");
    child.render("{% set bar = 'bar val' %}");

    EagerImportTag.integrateChild(
      child2Alias,
      child2.getContext().getSessionBindings(),
      child2,
      child
    );
    EagerImportTag.integrateChild(
      CONTEXT_VAR,
      child.getContext().getSessionBindings(),
      child,
      interpreter
    );
    assertThat(interpreter.getContext().get(CONTEXT_VAR)).isInstanceOf(Map.class);
    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get(child2Alias)
      )
      .isInstanceOf(Map.class);
    assertThat(
        (
          (Map<String, Object>) (
            (Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)
          ).get(child2Alias)
        ).get("foo")
      )
      .isEqualTo("foo val");

    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get("bar")
      )
      .isEqualTo("bar val");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itHandlesMultiLayerAliasedAndDeferred() {
    String child2Alias = "double_child";
    JinjavaInterpreter child = getChildInterpreter(interpreter, CONTEXT_VAR);
    JinjavaInterpreter child2 = getChildInterpreter(child, child2Alias);

    child2.render("{% set foo = 'foo val' %}");
    child.render("{% set bar = 'bar val' %}");
    child2.render("{% set foo_d = deferred %}");

    EagerImportTag.integrateChild(
      child2Alias,
      child2.getContext().getSessionBindings(),
      child2,
      child
    );
    EagerImportTag.integrateChild(
      CONTEXT_VAR,
      child.getContext().getSessionBindings(),
      child,
      interpreter
    );
    assertThat(interpreter.getContext().get(CONTEXT_VAR)).isInstanceOf(PyMap.class);
    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get(child2Alias)
      )
      .isInstanceOf(DeferredValue.class);
    assertThat(
        (
          (
            (Map<String, Object>) (
              (DeferredValue) (
                (Map<String, Object>) (interpreter.getContext().get(CONTEXT_VAR))
              ).get(child2Alias)
            ).getOriginalValue()
          ).get("foo")
        )
      )
      .isEqualTo("foo val");

    assertThat(
        (((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get("bar"))
      )
      .isEqualTo("bar val");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itHandlesMultiLayerAliasedAndNullDeferred() {
    String child2Alias = "double_child";
    JinjavaInterpreter child = getChildInterpreter(interpreter, CONTEXT_VAR);
    JinjavaInterpreter child2 = getChildInterpreter(child, child2Alias);

    child2.render("{% set foo = 'foo val' %}");
    child.render("{% set bar = 'bar val' %}");
    child2.render("{% set foo_d = deferred %}");

    EagerImportTag.integrateChild(
      child2Alias,
      child2.getContext().getSessionBindings(),
      child2,
      child
    );
    EagerImportTag.integrateChild(
      CONTEXT_VAR,
      child.getContext().getSessionBindings(),
      child,
      interpreter
    );
    assertThat(interpreter.getContext().get(CONTEXT_VAR)).isInstanceOf(PyMap.class);
    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get(child2Alias)
      )
      .isInstanceOf(DeferredValue.class);
    assertThat(
        (
          (
            (Map<String, Object>) (
              (DeferredValue) (
                (Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)
              ).get(child2Alias)
            ).getOriginalValue()
          ).get("foo")
        )
      )
      .isEqualTo("foo val");

    assertThat(
        (((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get("bar"))
      )
      .isEqualTo("bar val");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itHandlesMultiLayerDeferred() {
    JinjavaInterpreter child = getChildInterpreter(interpreter, "");
    JinjavaInterpreter child2 = getChildInterpreter(child, "");
    child2.getContext().put("foo", DeferredValue.instance("foo val"));
    child.getContext().put("bar", DeferredValue.instance("bar val"));

    EagerImportTag.integrateChild(
      "",
      child2.getContext().getSessionBindings(),
      child2,
      child
    );
    EagerImportTag.integrateChild(
      "",
      child.getContext().getSessionBindings(),
      child,
      interpreter
    );
    assertThat(interpreter.getContext().get("foo")).isInstanceOf(DeferredValue.class);
    assertThat(
        (((DeferredValue) (interpreter.getContext().get("foo"))).getOriginalValue())
      )
      .isEqualTo("foo val");

    assertThat(interpreter.getContext().get("bar")).isInstanceOf(DeferredValue.class);
    assertThat(
        (((DeferredValue) (interpreter.getContext().get("bar"))).getOriginalValue())
      )
      .isEqualTo("bar val");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itHandlesMultiLayerSomeAliased() {
    String child2Alias = "";
    String child3Alias = "triple_child";
    JinjavaInterpreter child = getChildInterpreter(interpreter, CONTEXT_VAR);
    JinjavaInterpreter child2 = getChildInterpreter(child, child2Alias);
    JinjavaInterpreter child3 = getChildInterpreter(child2, child3Alias);

    child2.render("{% set foo = 'foo val' %}");
    child.render("{% set bar = 'bar val' %}");
    child3.render("{% set foobar = 'foobar val' %}");

    EagerImportTag.integrateChild(
      child3Alias,
      child3.getContext().getSessionBindings(),
      child3,
      child2
    );
    EagerImportTag.integrateChild(
      child2Alias,
      child2.getContext().getSessionBindings(),
      child2,
      child
    );
    EagerImportTag.integrateChild(
      CONTEXT_VAR,
      child.getContext().getSessionBindings(),
      child,
      interpreter
    );
    assertThat(interpreter.getContext().get(CONTEXT_VAR)).isInstanceOf(Map.class);
    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get(child3Alias)
      )
      .isInstanceOf(Map.class);
    assertThat(
        (
          (Map<String, Object>) (
            (Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)
          ).get(child3Alias)
        ).get("foobar")
      )
      .isEqualTo("foobar val");

    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get("bar")
      )
      .isEqualTo("bar val");
    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get("foo")
      )
      .isEqualTo("foo val");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itHandlesMultiLayerAliasedAndParallel() {
    String child2Alias = "double_child";
    String child2BAlias = "double_child_b";

    JinjavaInterpreter child = getChildInterpreter(interpreter, CONTEXT_VAR);
    JinjavaInterpreter child2 = getChildInterpreter(child, child2Alias);
    JinjavaInterpreter child2B = getChildInterpreter(child, child2BAlias);

    child2.render("{% set foo = 'foo val' %}");
    child.render("{% set bar = 'bar val' %}");
    child2B.render("{% set foo_b = 'foo_b val' %}");

    EagerImportTag.integrateChild(
      child2Alias,
      child2.getContext().getSessionBindings(),
      child2,
      child
    );
    EagerImportTag.integrateChild(
      child2BAlias,
      child2B.getContext().getSessionBindings(),
      child2B,
      child
    );
    EagerImportTag.integrateChild(
      CONTEXT_VAR,
      child.getContext().getSessionBindings(),
      child,
      interpreter
    );
    assertThat(interpreter.getContext().get(CONTEXT_VAR)).isInstanceOf(Map.class);
    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get(child2Alias)
      )
      .isInstanceOf(Map.class);
    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get(
            child2BAlias
          )
      )
      .isInstanceOf(Map.class);
    assertThat(
        (
          (Map<String, Object>) (
            (Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)
          ).get(child2Alias)
        ).get("foo")
      )
      .isEqualTo("foo val");
    assertThat(
        (
          (Map<String, Object>) (
            (Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)
          ).get(child2BAlias)
        ).get("foo_b")
      )
      .isEqualTo("foo_b val");

    assertThat(
        ((Map<String, Object>) interpreter.getContext().get(CONTEXT_VAR)).get("bar")
      )
      .isEqualTo("bar val");
  }

  @Test
  public void itHandlesTripleLayer() {
    setupResourceLocator();
    context.put("a_val", "a");
    context.put("b_val", "b");
    context.put("c_val", "c");
    interpreter.render("{% import 'import-tree-c.jinja' as c %}");
    assertThat(interpreter.render("{{ c.b.a.foo_a }}")).isEqualTo("a");
    assertThat(interpreter.render("{{ c.b.foo_b }}")).isEqualTo("ba");
    assertThat(interpreter.render("{{ c.foo_c }}")).isEqualTo("cbaa");
  }

  @Test
  public void itDefersTripleLayer() {
    setupResourceLocator();
    context.put("a_val", DeferredValue.instance("a"));
    context.put("b_val", "b");
    context.put("c_val", "c");
    String result = interpreter.render("{% import 'import-tree-c.jinja' as c %}{{ c }}");
    assertThat(interpreter.render("{{ c.b.a.foo_a }}")).isEqualTo("{{ c.b.a.foo_a }}");
    assertThat(interpreter.render("{{ c.b.foo_b }}")).isEqualTo("{{ c.b.foo_b }}");
    assertThat(interpreter.render("{{ c.foo_c }}")).isEqualTo("{{ c.foo_c }}");
    context.put("a_val", "a");
    // There are some extras due to deferred values copying up the context stack.
    assertThat(interpreter.render(result).trim())
      .isEqualTo(
        "{'b':{'foo_b':'ba','a':{'foo_a':'a','something':'somn'},'foo_a':'a'}" +
        ",'foo_c':'cbaa','a':{'foo_a':'a','something':'somn'},'foo_b':'ba','foo_a':'a'}"
      );
  }

  @Test
  public void itHandlesQuadLayer() {
    setupResourceLocator();
    context.put("a_val", "a");
    context.put("b_val", "b");
    context.put("c_val", "c");
    interpreter.render("{% import 'import-tree-d.jinja' as d %}");
    assertThat(interpreter.render("{{ d.foo_d }}")).isEqualTo("cbaabaa");
    assertThat(interpreter.render("{{ d.resolvable }}")).isEqualTo("12345");
    assertThat(interpreter.render("{{ d.bar }}")).isEqualTo("cbaabaaba");
  }

  @Test
  public void itDefersQuadLayer() {
    setupResourceLocator();
    context.put("a_val", DeferredValue.instance("a"));
    context.put("b_val", "b");
    context.put("c_val", "c");
    String result = interpreter.render(
      "{% import 'import-tree-d.jinja' as d %}{{ d.resolvable }} {{ d.bar }}"
    );
    context.put("a_val", "a");
    assertThat(interpreter.render(result).trim()).isEqualTo("12345 cbaabaaba");
  }

  private static JinjavaInterpreter getChildInterpreter(
    JinjavaInterpreter interpreter,
    String alias
  ) {
    JinjavaInterpreter child = interpreter
      .getConfig()
      .getInterpreterFactory()
      .newInstance(interpreter);
    child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, TEMPLATE_FILE);
    EagerImportTag.setupImportAlias(alias, child, interpreter);
    return child;
  }

  private void setupResourceLocator() {
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
            Resources.getResource(String.format("tags/eager/importtag/%s", fullName)),
            StandardCharsets.UTF_8
          );
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );
  }

  @Test
  @Ignore
  @Override
  public void itReconstructsDeferredImportTag() {}

  @Test
  @Ignore
  @Override
  public void itDoesNotRenderTagsDependingOnDeferredImport() {}

  @Test
  @Ignore
  @Override
  public void itAddsAllDeferredNodesOfImport() {}

  @Test
  @Ignore
  @Override
  public void itAddsAllDeferredNodesOfGlobalImport() {}

  @Test
  @Ignore
  @Override
  public void itSetsErrorLineNumbersCorrectly() {}

  @Test
  @Ignore
  @Override
  public void itSetsErrorLineNumbersCorrectlyForImportedMacros() {}

  @Test
  @Ignore
  @Override
  public void itDefersImportedVariableKey() {}

  @Test
  @Ignore
  @Override
  public void itDoesNotRenderTagsDependingOnDeferredGlobalImport() {}

  @Test
  @Ignore
  @Override
  public void itSetsErrorLineNumbersCorrectlyThroughIncludeTag() {}
}
