package com.hubspot.jinjava.lib.tag.eager.importing;

import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.util.Map;
import java.util.stream.Collectors;

public interface EagerImportingStrategy {
  String handleDeferredTemplateFile(DeferredValueException e);
  void setup(JinjavaInterpreter child);

  void integrateChild(JinjavaInterpreter child);
  String getFinalOutput(
    String newPathSetter,
    String output,
    Map<String, Object> childBindings
  );

  static String getSetTagForDeferredChildBindings(
    JinjavaInterpreter interpreter,
    String currentImportAlias,
    Map<String, Object> childBindings
  ) {
    return childBindings
      .entrySet()
      .stream()
      .filter(
        entry ->
          entry.getValue() instanceof DeferredValue &&
          ((DeferredValue) entry.getValue()).getOriginalValue() != null
      )
      .filter(entry -> !interpreter.getContext().containsKey(entry.getKey()))
      .filter(entry -> !entry.getKey().equals(currentImportAlias))
      .map(
        entry ->
          EagerReconstructionUtils.buildBlockOrInlineSetTag( // don't register deferred token so that we don't defer them on higher context scopes; they only exist in the child scope
            entry.getKey(),
            ((DeferredValue) entry.getValue()).getOriginalValue(),
            interpreter
          )
      )
      .collect(Collectors.joining());
  }
}
