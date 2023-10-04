package com.hubspot.jinjava.lib.tag.eager.importing;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class AliasedEagerImportingStrategy implements EagerImportingStrategy {
  private static final String TEMPORARY_IMPORT_ALIAS_FORMAT = "__temp_import_alias_%d__";

  public static Optional<String> getTemporaryImportAlias(Context context) {
    return context
      .getImportResourceAlias()
      .map(AliasedEagerImportingStrategy::getTemporaryImportAlias);
  }

  private static String getTemporaryImportAlias(String fullAlias) {
    return String.format(
      TEMPORARY_IMPORT_ALIAS_FORMAT,
      Math.abs(Objects.hashCode(fullAlias))
    );
  }

  private final ImportingData importingData;
  private final String currentImportAlias;
  private final String fullImportAlias;

  @VisibleForTesting
  public AliasedEagerImportingStrategy(
    ImportingData importingData,
    String currentImportAlias
  ) {
    this.importingData = importingData;
    this.currentImportAlias = currentImportAlias;
    Optional<String> maybeParentImportAlias = importingData
      .getOriginalInterpreter()
      .getContext()
      .getImportResourceAlias();
    if (maybeParentImportAlias.isPresent()) {
      fullImportAlias =
        String.format("%s.%s", maybeParentImportAlias.get(), currentImportAlias);
    } else {
      fullImportAlias = currentImportAlias;
    }
  }

  @Override
  public String handleDeferredTemplateFile(DeferredValueException e) {
    return (
      importingData.getInitialPathSetter() +
      new PrefixToPreserveState(
        EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
          importingData.getOriginalInterpreter(),
          DeferredToken
            .builderFromToken(importingData.getTagToken())
            .addUsedDeferredWords(Stream.of(importingData.getHelpers().get(0)))
            .addSetDeferredWords(Stream.of(currentImportAlias))
            .build()
        )
      ) +
      importingData.getTagToken().getImage()
    );
  }

  @Override
  public void setup(JinjavaInterpreter child) {
    child.getContext().getScope().put(Context.IMPORT_RESOURCE_ALIAS_KEY, fullImportAlias);
    child.getContext().put(Context.IMPORT_RESOURCE_ALIAS_KEY, fullImportAlias);
    constructFullAliasPathMap(currentImportAlias, child);
    Map<String, Object> currentContextAliasMap = getMapForCurrentContextAlias(
      currentImportAlias,
      child
    );
    importingData
      .getOriginalInterpreter()
      .getContext()
      .put(getTemporaryImportAlias(fullImportAlias), DeferredValue.instance());
  }

  @Override
  public void integrateChild(JinjavaInterpreter child) {
    JinjavaInterpreter parent = importingData.getOriginalInterpreter();
    for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
      if (parent.getContext().isDeferredExecutionMode()) {
        macro.setDeferred(true);
      }
    }
    Map<String, Object> childBindings = child.getContext().getSessionBindings();
    childBindings.putAll(child.getContext().getGlobalMacros());
    String temporaryImportAlias = getTemporaryImportAlias(fullImportAlias);
    Map<String, Object> mapForCurrentContextAlias = getMapForCurrentContextAlias(
      currentImportAlias,
      child
    );
    // Remove layers from self down to original import alias to prevent reference loops
    childBindings.remove(temporaryImportAlias);
    importingData.getOriginalInterpreter().getContext().remove(temporaryImportAlias);
    // Remove meta keys
    childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
    childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
    mapForCurrentContextAlias.putAll(childBindings);
  }

  @Override
  public String getFinalOutput(
    String newPathSetter,
    String output,
    JinjavaInterpreter child
  ) {
    String temporaryImportAlias = getTemporaryImportAlias(fullImportAlias);
    return (
      newPathSetter +
      EagerReconstructionUtils.buildBlockOrInlineSetTag(
        temporaryImportAlias,
        Collections.emptyMap(),
        importingData.getOriginalInterpreter()
      ) +
      wrapInChildScope(
        EagerImportingStrategy.getSetTagForDeferredChildBindings(
          importingData.getOriginalInterpreter(),
          currentImportAlias,
          child.getContext()
        ) +
        output,
        child
      ) +
      EagerReconstructionUtils.buildSetTag(
        ImmutableMap.of(currentImportAlias, temporaryImportAlias),
        importingData.getOriginalInterpreter(),
        true
      ) +
      importingData.getInitialPathSetter()
    );
  }

  @SuppressWarnings("unchecked")
  private static void constructFullAliasPathMap(
    String currentImportAlias,
    JinjavaInterpreter child
  ) {
    String fullImportAlias = child
      .getContext()
      .getImportResourceAlias()
      .orElse(currentImportAlias);
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
        throw new InterpretException("Encountered a problem with import alias maps");
      }
    }
    currentMap.put(
      allAliases[allAliases.length - 1],
      child.getContext().isDeferredExecutionMode()
        ? DeferredValue.instance(new PyMap(new HashMap<>()))
        : new PyMap(new HashMap<>())
    );
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getMapForCurrentContextAlias(
    String currentImportAlias,
    JinjavaInterpreter child
  ) {
    Object parentValueForChild = child
      .getContext()
      .getParent()
      .getSessionBindings()
      .get(currentImportAlias);
    if (parentValueForChild instanceof Map) {
      return (Map<String, Object>) parentValueForChild;
    } else if (parentValueForChild instanceof DeferredValue) {
      if (((DeferredValue) parentValueForChild).getOriginalValue() instanceof Map) {
        return (Map<String, Object>) (
          (DeferredValue) parentValueForChild
        ).getOriginalValue();
      }
      Map<String, Object> newMap = new PyMap(new HashMap<>());
      child
        .getContext()
        .getParent()
        .put(currentImportAlias, DeferredValue.instance(newMap));
      return newMap;
    } else {
      Map<String, Object> newMap = new PyMap(new HashMap<>());
      child
        .getContext()
        .getParent()
        .put(
          currentImportAlias,
          child.getContext().isDeferredExecutionMode()
            ? DeferredValue.instance(newMap)
            : newMap
        );
      return newMap;
    }
  }

  private String wrapInChildScope(String output, JinjavaInterpreter child) {
    String combined =
      output + getDoTagToPreserve(importingData.getOriginalInterpreter(), child);
    // So that any set variables other than the alias won't exist outside the child's scope
    return EagerReconstructionUtils.wrapInChildScope(
      combined,
      importingData.getOriginalInterpreter()
    );
  }

  private String getDoTagToPreserve(
    JinjavaInterpreter interpreter,
    JinjavaInterpreter child
  ) {
    StringJoiner keyValueJoiner = new StringJoiner(",");
    String temporaryImportAlias = getTemporaryImportAlias(fullImportAlias);
    Map<String, Object> currentAliasMap = getMapForCurrentContextAlias(
      currentImportAlias,
      child
    );
    for (Map.Entry<String, Object> entry : currentAliasMap.entrySet()) {
      if (entry.getKey().equals(temporaryImportAlias)) {
        continue;
      }
      if (entry.getValue() instanceof DeferredValue) {
        keyValueJoiner.add(String.format("'%s': %s", entry.getKey(), entry.getKey()));
      } else if (!(entry.getValue() instanceof MacroFunction)) {
        keyValueJoiner.add(
          String.format(
            "'%s': %s",
            entry.getKey(),
            PyishObjectMapper.getAsPyishString(entry.getValue())
          )
        );
      }
    }
    if (keyValueJoiner.length() > 0) {
      return EagerReconstructionUtils.buildDoUpdateTag(
        temporaryImportAlias,
        "{" + keyValueJoiner.toString() + "}",
        interpreter
      );
    }
    return "";
  }
}
