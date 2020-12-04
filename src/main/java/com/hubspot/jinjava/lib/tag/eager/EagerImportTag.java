package com.hubspot.jinjava.lib.tag.eager;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.hubspot.jinjava.util.ChunkResolver;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
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
      } else if (!Strings.isNullOrEmpty(contextVar)) {
        // Start it as a new dictionary before output
        output += getDoTagToPreserve(interpreter, contextVar);
        //          buildSetTagForDeferredInChildContext(
        //            ImmutableMap.of(
        //              contextVar,
        //              ChunkResolver.getValueAsJinjavaString(
        //                (
        //                  (DeferredValue) interpreter.getContext().get(contextVar)
        //                ).getOriginalValue()
        //              )
        //            ),
        //            interpreter,
        //            true
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

  @SuppressWarnings("unchecked")
  private static String getDoTagToPreserve(
    JinjavaInterpreter interpreter,
    String contextVar
  )
    throws JsonProcessingException {
    StringJoiner joiner = new StringJoiner(",");
    for (Map.Entry<String, Object> entry : (
      (Map<String, Object>) (
        (DeferredValue) interpreter.getContext().get(contextVar)
      ).getOriginalValue()
    ).entrySet()) {
      if (entry.getValue() instanceof DeferredValue) {
        joiner.add(String.format("'%s': %s", entry.getKey(), entry.getKey()));
      } else if (!(entry.getValue() instanceof MacroFunction)) {
        joiner.add(
          String.format(
            "'%s': %s",
            entry.getKey(),
            ChunkResolver.getValueAsJinjavaString(entry.getValue())
          )
        );
      }
    }
    if (joiner.length() > 0) {
      return String.format("{%% do %s.update({%s}) %%}", contextVar, joiner.toString());
    }
    return "";
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
      putNewMapForAlias(contextVar, child);
      getMapForAlias(contextVar, child, false);
    }
  }

  @SuppressWarnings("unchecked")
  private static void putNewMapForAlias(String contextVar, JinjavaInterpreter child) {
    String fullImportAlias = child
      .getContext()
      .getImportResourceAlias()
      .orElse(contextVar);
    String[] allAliases = fullImportAlias.split("\\.");
    Map<String, Object> currentMap = child.getContext().getParent();
    for (int i = 0; i < allAliases.length - 1; i++) {
      Object maybeNextMap = currentMap.get(allAliases[i]);
      if (maybeNextMap instanceof Map) {
        currentMap = (Map<String, Object>) maybeNextMap;
      } else if (
        maybeNextMap instanceof DeferredValue &&
        ((DeferredValue) maybeNextMap).getOriginalValue() instanceof Map
      ) {
        currentMap =
          (Map<String, Object>) ((DeferredValue) maybeNextMap).getOriginalValue();
      } else {
        return;
      }
    }
    currentMap.put(allAliases[allAliases.length - 1], new PyMap(new HashMap<>()));
    //    Object parentValueForChild = child.getContext().getParent().get(contextVar);
    //    if (parentValueForChild instanceof Map) {
    //      return;
    //    } else if (parentValueForChild instanceof DeferredValue) {
    //      if (((DeferredValue) parentValueForChild).getOriginalValue() instanceof Map) {
    //        return;
    //      }
    //      child
    //        .getContext()
    //        .getParent()
    //        .put(contextVar, DeferredValue.instance(new PyMap(new HashMap<>())));
    //    } else {
    //      child.getContext().getParent().put(contextVar, new PyMap(new HashMap<>()));
    //    }
  }

  @SuppressWarnings("unchecked")
  private static Optional<Map<String, Object>> getMapForAlias(
    String contextVar,
    JinjavaInterpreter child,
    boolean convertToDeferredValue
  ) {
    //    String fullImportAlias = child
    //      .getContext()
    //      .getImportResourceAlias()
    //      .orElse(contextVar);
    //    String[] allAliases = fullImportAlias.split("\\.");
    //    Map<String, Object> currentMap = child.getContext();
    //    for (int i = 0; i < allAliases.length; i++) {
    //      Object maybeNextMap = currentMap.get(allAliases[i]);
    //      if (maybeNextMap instanceof Map) {
    //        currentMap = (Map<String, Object>) maybeNextMap;
    //      } else if (
    //        maybeNextMap instanceof DeferredValue &&
    //        ((DeferredValue) maybeNextMap).getOriginalValue() instanceof Map
    //      ) {
    //        currentMap =
    //          (Map<String, Object>) ((DeferredValue) maybeNextMap).getOriginalValue();
    //      } else {
    //        return Optional.empty();
    //      }
    //    }
    Object parentValueForChild = child.getContext().getParent().get(contextVar);
    if (parentValueForChild instanceof Map) {
      if (convertToDeferredValue) {
        child
          .getContext()
          .getParent()
          .put(contextVar, DeferredValue.instance(parentValueForChild));
      }
      return Optional.of((Map<String, Object>) parentValueForChild);
    } else if (parentValueForChild instanceof DeferredValue) {
      if (((DeferredValue) parentValueForChild).getOriginalValue() instanceof Map) {
        return Optional.of(
          (Map<String, Object>) ((DeferredValue) parentValueForChild).getOriginalValue()
        );
      }
      Map<String, Object> newMap = new PyMap(new HashMap<>());
      child.getContext().getParent().put(contextVar, DeferredValue.instance(newMap));
      return Optional.of(newMap);
    } else {
      Map<String, Object> newMap = new PyMap(new HashMap<>());
      if (convertToDeferredValue) {
        child.getContext().getParent().put(contextVar, DeferredValue.instance(newMap));
      } else {
        child.getContext().getParent().put(contextVar, newMap);
      }
      return Optional.of(newMap);
    }
    //    return Optional.of(currentMap);
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
      //      putBindingsOnContext("", childBindings, parent);
    } else {
      // Since we might be multiple layers deep of importing, we need the full name.
      String fullImportAlias = child
        .getContext()
        .getImportResourceAlias()
        .orElse(contextVar);
      //      Object aliasMap = childBindings.get(fullImportAlias);
      Map<String, MacroFunction> globalMacros = child.getContext().getGlobalMacros();
      for (Map.Entry<String, MacroFunction> macro : globalMacros.entrySet()) {
        childBindings.put(macro.getKey(), macro.getValue());
      }

      //      if (aliasMap instanceof DeferredValue) {
      //        childBindings.putAll(getDeferredAliasMap((DeferredValue) aliasMap));
      //        //        putBindingsOnContext(
      //        //          contextVar,
      //        //          getDeferredAliasMap(fullImportAlias, childBindings),
      //        //          parent
      //        //        );
      //      } else {
      //        //        putBindingsOnContext(
      //        //          contextVar,
      //        //          (Map<String, Object>) childBindings.get(fullImportAlias),
      //        //          parent
      //        //        );
      //        for (Map.Entry<String, Object> aliasBinding : (
      //          (Map<String, Object>) aliasMap
      //        ).entrySet()) {
      //          childBindings.put(aliasBinding.getKey(), aliasBinding.getValue());
      //        }
      //      }
      //      childBindings.remove(fullImportAlias);
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      //      childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
      childBindings.remove(Context.IMPORT_RESOURCE_PATH_KEY);
      getMapForAlias(
          contextVar,
          child,
          childBindings.get(fullImportAlias.split("\\.", 2)[0]) instanceof DeferredValue
        )
        .ifPresent(
          map -> {
            //
            childBindings.remove(fullImportAlias.split("\\.", 2)[0]);
            childBindings.remove(contextVar);

            childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
            map.putAll(childBindings);
            //            parent.getContext().put(contextVar, childBindings);
          }
        );
      //      putBindingsOnContext(contextVar, childBindings, parent);
      //      parent.getContext().putAll(childBindings);
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
    Object existingValueForContextVar = parent.getContext().get(contextVar);
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
      if (Strings.isNullOrEmpty(contextVar)) {
        mapToFlattenOnto = parent.getContext();
      } else if (existingValueForContextVar == null) {
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
    if (!Strings.isNullOrEmpty(contextVar)) {
      mapToPutOnDirectly.put(contextVar, childBindings);
    } else {
      mapToPutOnDirectly.putAll(childBindings);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getDeferredAliasMap(DeferredValue aliasMap) {
    Map<String, Object> allDeferred = new HashMap<>();
    if (aliasMap.getOriginalValue() instanceof Map) {
      for (Map.Entry<String, Object> deferredBinding : (
        (Map<String, Object>) aliasMap.getOriginalValue()
      ).entrySet()) {
        allDeferred.put(
          deferredBinding.getKey(),
          deferredBinding.getValue() instanceof DeferredValue
            ? deferredBinding.getValue()
            : DeferredValue.instance(deferredBinding.getValue())
        );
      }
    }
    return allDeferred;
  }
}
