package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class EagerImportTag extends EagerStateChangingTag<ImportTag> {

  public EagerImportTag() {
    super(new ImportTag());
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    List<String> helper = ImportTag.getHelpers(tagToken);

    String contextVar = ImportTag.getContextVar(helper);

    Optional<String> maybeTemplateFile = ImportTag.getTemplateFile(
      helper,
      tagToken,
      interpreter
    );
    if (!maybeTemplateFile.isPresent()) {
      return "";
    }
    String templateFile = maybeTemplateFile.get();
    try {
      Node node = ImportTag.parseTemplateAsNode(interpreter, templateFile);

      JinjavaInterpreter child = interpreter
        .getConfig()
        .getInterpreterFactory()
        .newInstance(interpreter);
      child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile);

      JinjavaInterpreter.pushCurrent(child);
      setupImportAlias(contextVar, child, interpreter);
      String output;
      try {
        output = child.render(node);
      } finally {
        JinjavaInterpreter.popCurrent();
      }

      interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());

      Map<String, Object> childBindings = child.getContext().getSessionBindings();

      // If the template depends on deferred values it should not be rendered and all defined variables and macros should be deferred too
      if (!child.getContext().getDeferredNodes().isEmpty()) {
        ImportTag.handleDeferredNodesDuringImport(
          tagToken,
          node,
          contextVar,
          templateFile,
          childBindings,
          child,
          interpreter
        );
      }
      Optional<String> maybeImportAlias = child.getContext().getImportResourceAlias();
      integrateChild(contextVar, childBindings, child, interpreter);
      if (child.getContext().getEagerTokens().isEmpty() || output == null) {
        output = "";
      } else if (maybeImportAlias.isPresent()) {
        // Start it as a new dictionary before output
        output =
          buildSetTagForDeferredInChildContext(
            ImmutableMap.of(maybeImportAlias.get(), "{}"),
            interpreter,
            true
          ) +
          output;
      }
      return output;
    } catch (IOException e) {
      throw new InterpretException(
        e.getMessage(),
        e,
        tagToken.getLineNumber(),
        tagToken.getStartPosition()
      );
    } finally {
      interpreter.getContext().getCurrentPathStack().pop();
    }
  }

  @VisibleForTesting
  public static void setupImportAlias(
    String contextVar,
    JinjavaInterpreter child,
    JinjavaInterpreter parent
  ) {
    if (!Strings.isNullOrEmpty(contextVar)) {
      Optional<String> maybeParentImportAlias = parent
        .getContext()
        .getImportResourceAlias();
      if (maybeParentImportAlias.isPresent()) {
        child
          .getContext()
          .getScope()
          .put(
            Context.IMPORT_RESOURCE_ALIAS_KEY,
            String.format("%s.%s", maybeParentImportAlias.get(), contextVar)
          );
      } else {
        child.getContext().getScope().put(Context.IMPORT_RESOURCE_ALIAS_KEY, contextVar);
      }
      child
        .getContext()
        .getScope()
        .put(
          String.valueOf(child.getContext().get(Context.IMPORT_RESOURCE_ALIAS_KEY)),
          new PyMap(new HashMap<>())
        );
    }
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  public static void integrateChild(
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter child,
    JinjavaInterpreter parent
  ) {
    if (StringUtils.isBlank(contextVar)) {
      for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
        parent.getContext().addGlobalMacro(macro);
      }
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      parent.getContext().putAll(childBindings);
    } else {
      // Since we might be multiple layers deep of importing, we need the full name.
      String fullImportAlias = child
        .getContext()
        .getImportResourceAlias()
        .orElse(contextVar);
      for (Map.Entry<String, MacroFunction> macro : child
        .getContext()
        .getGlobalMacros()
        .entrySet()) {
        childBindings.put(macro.getKey(), macro.getValue());
      }

      if (childBindings.get(fullImportAlias) instanceof DeferredValue) {
        flattenDeferredContextVar(fullImportAlias, childBindings);
      } else {
        for (Map.Entry<String, Object> aliasBinding : (
          (Map<String, Object>) childBindings.get(fullImportAlias)
        ).entrySet()) {
          childBindings.put(aliasBinding.getKey(), aliasBinding.getValue());
        }
      }
      childBindings.remove(fullImportAlias);
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
      putBindingsOnContext(contextVar, childBindings, parent);
    }
  }

  @SuppressWarnings("unchecked")
  private static void putBindingsOnContext(
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter parent
  ) {
    Optional<String> maybeParentImportAlias = parent
      .getContext()
      .getImportResourceAlias();
    if (maybeParentImportAlias.isPresent()) {
      putOntoAliasedParentContext(
        contextVar,
        childBindings,
        parent,
        maybeParentImportAlias.get()
      );
    } else {
      putOntoParentContext(contextVar, childBindings, parent);
    }
  }

  private static void putOntoParentContext(
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter parent
  ) {
    Map<String, Object> mapToFlattenOnto;
    Object existingValueForContextVar = parent.getContext().getScope().get(contextVar);
    if (existingValueForContextVar instanceof DeferredValue) {
      Object originalValue =
        ((DeferredValue) existingValueForContextVar).getOriginalValue();
      if (originalValue instanceof Map) {
        mapToFlattenOnto = ((Map<String, Object>) originalValue);
      } else {
        mapToFlattenOnto = new PyMap(new HashMap<>());
        parent.getContext().put(contextVar, DeferredValue.instance(mapToFlattenOnto));
      }
    } else {
      if (existingValueForContextVar == null) {
        // If it is, make it a new map instead.
        mapToFlattenOnto = new PyMap(new HashMap<>());
        parent.getContext().put(contextVar, mapToFlattenOnto);
      } else {
        mapToFlattenOnto = ((Map<String, Object>) existingValueForContextVar);
      }
    }
    mapToFlattenOnto.putAll(childBindings);
  }

  private static void putOntoAliasedParentContext(
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter parent,
    String parentImportAlias
  ) {
    Map<String, Object> mapToPutOnDirectly;
    // More than one level deep
    Object parentAliasBindings = parent.getContext().get(parentImportAlias);
    if (parentAliasBindings instanceof DeferredValue) {
      // This has already been deferred
      Object originalValue = ((DeferredValue) parentAliasBindings).getOriginalValue();
      if (originalValue instanceof Map) {
        // Since the original is a map, we can add the child bindings to it.
        mapToPutOnDirectly = ((Map<String, Object>) originalValue);
      } else {
        // Otherwise we need to make it a map that we can add the child bindings to.
        mapToPutOnDirectly = new PyMap(new HashMap<>());
        parent
          .getContext()
          .put(parentImportAlias, DeferredValue.instance(mapToPutOnDirectly));
      }
    } else {
      if (parentAliasBindings == null) {
        // If somehow it's null, make it a new map instead.
        mapToPutOnDirectly = new PyMap(new HashMap<>());
        parent.getContext().put(parentImportAlias, mapToPutOnDirectly);
      } else {
        mapToPutOnDirectly = ((Map<String, Object>) parentAliasBindings);
      }
    }
    mapToPutOnDirectly.put(contextVar, childBindings);
  }

  @SuppressWarnings("unchecked")
  private static void flattenDeferredContextVar(
    String contextVar,
    Map<String, Object> childBindings
  ) {
    DeferredValue contextVarMap = (DeferredValue) childBindings.get(contextVar);
    if (contextVarMap.getOriginalValue() instanceof Map) {
      for (Map.Entry<String, Object> deferredBinding : (
        (Map<String, Object>) contextVarMap.getOriginalValue()
      ).entrySet()) {
        childBindings.put(
          deferredBinding.getKey(),
          deferredBinding.getValue() instanceof DeferredValue
            ? deferredBinding.getValue()
            : DeferredValue.instance(deferredBinding.getValue())
        );
      }
    }
  }
}
