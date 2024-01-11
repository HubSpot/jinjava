package com.hubspot.jinjava.lib.tag.eager.importing;

import com.google.common.annotations.VisibleForTesting;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class FlatEagerImportingStrategy implements EagerImportingStrategy {

  private final ImportingData importingData;

  @VisibleForTesting
  public FlatEagerImportingStrategy(ImportingData importingData) {
    this.importingData = importingData;
  }

  @Override
  public String handleDeferredTemplateFile(DeferredValueException e) {
    throw e;
  }

  @Override
  public void setup(JinjavaInterpreter child) {
    // Do nothing
  }

  @Override
  public void integrateChild(JinjavaInterpreter child) {
    JinjavaInterpreter parent = importingData.getOriginalInterpreter();
    for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
      if (parent.getContext().isDeferredExecutionMode()) {
        macro.setDeferred(true);
      }
    }
    for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
      parent.getContext().addGlobalMacro(macro);
    }
    Map<String, Object> childBindings = child.getContext().getSessionBindings();

    childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
    childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
    Map<String, Object> childBindingsWithoutImportResourcePath =
      ImportTag.getChildBindingsWithoutImportResourcePath(childBindings);
    if (parent.getContext().isDeferredExecutionMode()) {
      childBindingsWithoutImportResourcePath
        .keySet()
        .forEach(key ->
          parent
            .getContext()
            .put(key, DeferredValue.instance(parent.getContext().get(key)))
        );
    } else {
      parent.getContext().putAll(childBindingsWithoutImportResourcePath);
    }
  }

  @Override
  public String getFinalOutput(
    String newPathSetter,
    String output,
    JinjavaInterpreter child
  ) {
    if (importingData.getOriginalInterpreter().getContext().isDeferredExecutionMode()) {
      Set<String> metaContextVariables = importingData
        .getOriginalInterpreter()
        .getContext()
        .getMetaContextVariables();
      // defer imported variables
      EagerReconstructionUtils.buildSetTag(
        child
          .getContext()
          .getSessionBindings()
          .entrySet()
          .stream()
          .filter(entry ->
            !(entry.getValue() instanceof DeferredValue) && entry.getValue() != null
          )
          .filter(entry -> !metaContextVariables.contains(entry.getKey()))
          .collect(Collectors.toMap(Entry::getKey, entry -> "")),
        importingData.getOriginalInterpreter(),
        true
      );
    }
    return (
      newPathSetter +
      EagerImportingStrategy.getSetTagForDeferredChildBindings(
        importingData.getOriginalInterpreter(),
        null,
        child.getContext().getSessionBindings()
      ) +
      output +
      importingData.getInitialPathSetter()
    );
  }
}
