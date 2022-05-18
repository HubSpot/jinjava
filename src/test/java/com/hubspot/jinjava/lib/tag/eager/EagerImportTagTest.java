package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.tag.ImportTag;
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EagerImportTagTest extends ImportTagTest {
  private static final String CONTEXT_VAR = "context_var";
  private static final String TEMPLATE_FILE = "template.jinja";

  @Before
  public void eagerSetup() throws Exception {
    context.put("padding", 42);
    context.registerFilter(new PrintPathFilter());
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .withEnableRecursiveMacroCalls(true)
          .withMaxMacroRecursionDepth(10)
          .build()
      );
    Tag tag = EagerTagFactory
      .getEagerTagDecorator(new ImportTag())
      .orElseThrow(RuntimeException::new);
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
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
    String result = interpreter.render(
      "{% import 'import-tree-c.jinja' as c %}{{ c|dictsort(false, 'key') }}"
    );
    assertThat(interpreter.render("{{ c.b.a.foo_a }}")).isEqualTo("{{ c.b.a.foo_a }}");
    assertThat(interpreter.render("{{ c.b.foo_b }}")).isEqualTo("{{ c.b.foo_b }}");
    assertThat(interpreter.render("{{ c.foo_c }}")).isEqualTo("{{ c.foo_c }}");
    removeDeferredContextKeys();
    context.put("a_val", "a");
    // There are some extras due to deferred values copying up the context stack.
    assertThat(interpreter.render(result).trim())
      .isEqualTo(
        interpreter.render(
          "{% import 'import-tree-c.jinja' as c %}{{ c|dictsort(false, 'key') }}"
        )
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
    removeDeferredContextKeys();

    context.put("a_val", "a");
    assertThat(interpreter.render(result).trim()).isEqualTo("12345 cbaabaaba");
  }

  @Test
  public void itHandlesQuadLayerInDeferredIf() {
    setupResourceLocator();
    context.put("a_val", "a");
    context.put("b_val", "b");
    String result = interpreter.render(
      "{% if deferred %}{% import 'import-tree-b.jinja' as b %}{% endif %}"
    );
    assertThat(result)
      .isEqualTo(
        "{% if deferred %}{% set __ignored__ %}{% set current_path = 'import-tree-b.jinja' %}{% set a,foo_b = {'foo_a': 'a', 'import_resource_path': 'import-tree-a.jinja', 'something': 'somn'},null %}{% set b = {} %}{% set __ignored__ %}{% set current_path = 'import-tree-a.jinja' %}{% set a = {} %}{% set something = 'somn' %}{% do a.update({'something': something}) %}\n" +
        "{% set foo_a = 'a' %}{% do a.update({'foo_a': foo_a}) %}\n" +
        "{% do a.update({'foo_a': 'a','import_resource_path': 'import-tree-a.jinja','something': 'somn'}) %}{% set current_path = 'import-tree-b.jinja' %}{% endset %}\n" +
        "{% set foo_b = 'b' + a.foo_a %}{% do b.update({'foo_b': foo_b}) %}\n" +
        "{% do b.update({'a': a,'foo_b': foo_b,'import_resource_path': 'import-tree-b.jinja'}) %}{% set current_path = '' %}{% endset %}{% endif %}"
      );

    removeDeferredContextKeys();
    context.put("deferred", true);

    interpreter.render(result);
    assertThat(interpreter.render("{{ b.foo_b }}")).isEqualTo("ba");
    assertThat(interpreter.render("{{ b.a.foo_a }}")).isEqualTo("a");
  }

  @Test
  public void itCorrectlySetsAliasedPath() {
    setupResourceLocator();
    context.put("foo", "foo");
    String result = interpreter.render(
      "{% import 'import-macro.jinja' as m %}{{ m.print_path_macro(foo) }}"
    );
    assertThat(result.trim()).isEqualTo("import-macro.jinja\nfoo");
  }

  @Test
  public void itCorrectlySetsPath() {
    setupResourceLocator();
    context.put("foo", "foo");
    String result = interpreter.render(
      "{% import 'import-macro.jinja' %}{{ print_path_macro(foo) }}"
    );
    assertThat(result.trim()).isEqualTo("import-macro.jinja\nfoo");
  }

  @Test
  public void itCorrectlySetsAliasedPathForSecondPass() {
    setupResourceLocator();
    context.put("foo", DeferredValue.instance());
    String firstPassResult = interpreter.render(
      "{% import 'import-macro.jinja' as m %}{{ m.print_path_macro(foo) }}"
    );
    assertThat(firstPassResult)
      .isEqualTo(
        "{% set deferred_import_resource_path = 'import-macro.jinja' %}{% macro m.print_path_macro(var) %}\n" +
        "{{ filter:print_path.filter(var, ____int3rpr3t3r____) }}\n" +
        "{{ var }}\n" +
        "{% endmacro %}{% set deferred_import_resource_path = null %}{{ m.print_path_macro(foo) }}"
      );
    context.put("foo", "foo");
    assertThat(interpreter.render(firstPassResult).trim())
      .isEqualTo("import-macro.jinja\nfoo");
  }

  @Test
  public void itCorrectlySetsPathForSecondPass() {
    setupResourceLocator();
    context.put("foo", DeferredValue.instance());
    String firstPassResult = interpreter.render(
      "{% import 'import-macro.jinja' %}{{ print_path_macro(foo) }}"
    );
    assertThat(firstPassResult)
      .isEqualTo(
        "{% set deferred_import_resource_path = 'import-macro.jinja' %}{% macro print_path_macro(var) %}\n" +
        "{{ filter:print_path.filter(var, ____int3rpr3t3r____) }}\n" +
        "{{ var }}\n" +
        "{% endmacro %}{% set deferred_import_resource_path = null %}{{ print_path_macro(foo) }}"
      );
    context.put("foo", "foo");
    assertThat(interpreter.render(firstPassResult).trim())
      .isEqualTo("import-macro.jinja\nfoo");
  }

  @Test
  public void itCorrectlySetsNestedPathsForSecondPass() {
    setupResourceLocator();
    context.put("foo", DeferredValue.instance());
    String firstPassResult = interpreter.render(
      "{% import 'double-import-macro.jinja' %}{{ print_path_macro2(foo) }}"
    );
    assertThat(firstPassResult)
      .isEqualTo(
        "{% set deferred_import_resource_path = 'double-import-macro.jinja' %}{% macro print_path_macro2(var) %}{{ filter:print_path.filter(var, ____int3rpr3t3r____) }}\n" +
        "{% set deferred_import_resource_path = 'import-macro.jinja' %}{% macro print_path_macro(var) %}\n" +
        "{{ filter:print_path.filter(var, ____int3rpr3t3r____) }}\n" +
        "{{ var }}\n" +
        "{% endmacro %}{% set deferred_import_resource_path = 'double-import-macro.jinja' %}{{ print_path_macro(var) }}{% endmacro %}{% set deferred_import_resource_path = null %}{{ print_path_macro2(foo) }}"
      );
    context.put("foo", "foo");
    context.put(Context.GLOBAL_MACROS_SCOPE_KEY, null);
    assertThat(interpreter.render(firstPassResult).trim())
      .isEqualTo("double-import-macro.jinja\n\nimport-macro.jinja\nfoo");
  }

  @Test
  public void itImportsDoublyNamed() {
    setupResourceLocator();
    String result = interpreter.render(
      "{% import 'variables.jinja' as foo %}{{ foo.foo['foo'].bar }}"
    );
    assertThat(result).isEqualTo("here");
  }

  @Test
  public void itKeepsImportAliasesInsideOwnScope() {
    setupResourceLocator();
    String result = interpreter.render(
      "{% import 'printer-a.jinja' as printer %}{% import 'intermediate-b.jinja' as inter %}" +
      "{{ printer.print() }}-{{ inter.print() }}"
    );
    assertThat(result.trim()).isEqualTo("A_A_A-B_inter_B");
  }

  @Test
  public void itKeepsImportAliasVariablesInsideOwnScope() {
    setupResourceLocator();
    String result = interpreter.render(
      "{% set printer = {'key': 'val'} %}{% import 'intermediate-b.jinja' as inter %}" +
      "{{ printer }}-{{ inter.print() }}"
    );
    assertThat(result.trim()).isEqualTo("{'key': 'val'}-B_inter_B");
  }

  @Test
  public void itKeepsDeferredImportAliasesInsideOwnScope() {
    setupResourceLocator();
    String result = interpreter.render(
      "{% import 'printer-a.jinja' as printer %}{% import 'intermediate-b.jinja' as inter %}" +
      "{{ printer.print(deferred) }}-{{ inter.print(deferred) }}"
    );
    context.put("deferred", "resolved");
    assertThat(interpreter.render(result)).isEqualTo("A_resolved_A-B_resolved_B");
  }

  @Test
  public void itDefersWhenPathIsDeferred() {
    String input = "{% import deferred as foo %}";
    String output = interpreter.render(input);
    assertThat(output).isEqualTo("{% set current_path = '' %}" + input);
    assertThat(interpreter.getContext().get("foo"))
      .isNotNull()
      .isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itReconstructsCurrentPath() {
    interpreter.getContext().put(RelativePathResolver.CURRENT_PATH_CONTEXT_KEY, "bar");
    String input = "{% import deferred as foo %}";
    String output = interpreter.render(input);
    assertThat(output).isEqualTo("{% set current_path = 'bar' %}" + input);
    assertThat(interpreter.getContext().get("foo"))
      .isNotNull()
      .isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itDefersNodeWhenNoImportAlias() {
    String input = "{% import deferred %}";
    String output = interpreter.render(input);
    assertThat(output).isEqualTo(input);
    assertThat(interpreter.getContext().getDeferredNodes()).hasSize(1);
  }

  @Test
  public void itHandlesVarFromImportedMacro() {
    setupResourceLocator();
    String result = interpreter.render(
      "{% import 'import-macro-and-var.jinja' -%}\n" +
      "{{ adjust('a') }}\n" +
      "{{ adjust('b') }}\n" +
      "c{{ var }}"
    );
    assertThat(result.trim())
      .isEqualTo(
        "{% set var = [] %}{% do var.append('a' ~ deferred) %}\n" +
        "a\n" +
        "{% do var.append('b' ~ deferred) %}\n" +
        "b\n" +
        "c{{ var }}"
      );
    context.put("deferred", "resolved");
    assertThat(interpreter.render(result).trim())
      .isEqualTo("a\n" + "\n" + "b\n" + "c['aresolved', 'bresolved']");
  }

  @Test
  public void itPutsDeferredInOtherSpotValuesOnContext() {
    setupResourceLocator();
    String result = interpreter.render(
      "{% import 'import-macro-applies-val.jinja' -%}\n" +
      "{% set val = deferred -%}\n" +
      "{{ apply('foo') }}\n" +
      "{{ val }}"
    );
    assertThat(result.trim()).isEqualTo("{% set val = deferred %}\n5foo\n{{ val }}");
    context.put("deferred", "resolved");
    assertThat(interpreter.render(result).trim()).isEqualTo("5foo\nresolved");
  }

  @Test
  public void itDoesNotSilentlyOverrideMacro() {
    setupResourceLocator();
    String result = interpreter.render(
      "{% import 'macro-a.jinja' as macros %}\n" +
      "{{ macros.doer() }}\n" +
      "{% if deferred %}\n" +
      "  {% import 'macro-b.jinja' as macros %}\n" +
      "{% endif %}\n" +
      "{{ macros.doer() }}"
    );
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
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

  private void removeDeferredContextKeys() {
    context
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue() instanceof DeferredValue)
      .map(Entry::getKey)
      .collect(Collectors.toSet())
      .forEach(context::remove);
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

  public static class PrintPathFilter implements Filter {

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return interpreter.getContext().getCurrentPathStack().peek().orElse("/");
    }

    @Override
    public String getName() {
      return "print_path";
    }
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
