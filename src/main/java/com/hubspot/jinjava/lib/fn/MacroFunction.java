package com.hubspot.jinjava.lib.fn;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.TemporaryValueClosable;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Function definition parsed from a jinjava template, stored in global macros registry in interpreter context.
 *
 * @author jstehler
 *
 */
public class MacroFunction extends AbstractCallableMethod {
  private final List<Node> content;

  private final boolean caller;

  private final Context localContextScope;

  private final int definitionLineNumber;

  private final int definitionStartPosition;

  private boolean deferred;

  public MacroFunction(
    List<Node> content,
    String name,
    LinkedHashMap<String, Object> argNamesWithDefaults,
    boolean caller,
    Context localContextScope,
    int lineNumber,
    int startPosition
  ) {
    super(name, argNamesWithDefaults);
    this.content = content;
    this.caller = caller;
    this.localContextScope = localContextScope;
    this.definitionLineNumber = lineNumber;
    this.definitionStartPosition = startPosition;
    this.deferred = false;
  }

  public MacroFunction(MacroFunction source, String name) {
    super(name, (LinkedHashMap<String, Object>) source.getDefaults());
    this.content = source.content;
    this.caller = source.caller;
    this.localContextScope = source.localContextScope;
    this.definitionLineNumber = source.definitionLineNumber;
    this.definitionStartPosition = source.definitionStartPosition;
    this.deferred = source.deferred;
  }

  @Override
  public Object doEvaluate(
    Map<String, Object> argMap,
    Map<String, Object> kwargMap,
    List<Object> varArgs
  ) {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    Optional<String> importFile = getImportFile(interpreter);
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      interpreter.getContext().setDeferredExecutionMode(false);
      String result = getEvaluationResult(argMap, kwargMap, varArgs, interpreter);

      if (
        !interpreter.getContext().isPartialMacroEvaluation() &&
        (
          !interpreter.getContext().getDeferredNodes().isEmpty() ||
          !interpreter.getContext().getDeferredTokens().isEmpty()
        )
      ) {
        interpreter
          .getContext()
          .removeDeferredTokens(
            ImmutableList.copyOf(interpreter.getContext().getDeferredTokens())
          );
        // If the macro function could not be fully evaluated, throw a DeferredValueException.
        throw new DeferredValueException(
          getName(),
          interpreter.getLineNumber(),
          interpreter.getPosition()
        );
      }

      return result;
    } finally {
      importFile.ifPresent(path -> interpreter.getContext().getCurrentPathStack().pop());
    }
  }

  public Optional<String> getImportFile(JinjavaInterpreter interpreter) {
    Optional<String> importFile = Optional.ofNullable(
      (String) localContextScope.get(Context.IMPORT_RESOURCE_PATH_KEY)
    );

    // pushWithoutCycleCheck() is used to here so that macros calling macros from the same file will not throw a TagCycleException
    importFile.ifPresent(
      path ->
        interpreter
          .getContext()
          .getCurrentPathStack()
          .pushWithoutCycleCheck(
            path,
            interpreter.getLineNumber(),
            interpreter.getPosition()
          )
    );
    return importFile;
  }

  public String getEvaluationResult(
    Map<String, Object> argMap,
    Map<String, Object> kwargMap,
    List<Object> varArgs,
    JinjavaInterpreter interpreter
  ) {
    interpreter.setLineNumber(definitionLineNumber);
    interpreter.setPosition(definitionStartPosition);
    if (
      !Objects.equals(
        interpreter.getContext().get(Context.IMPORT_RESOURCE_PATH_KEY),
        localContextScope.get(Context.IMPORT_RESOURCE_PATH_KEY)
      )
    ) {
      for (Map.Entry<String, Object> scopeEntry : localContextScope
        .getScope()
        .entrySet()) {
        if (scopeEntry.getValue() instanceof MacroFunction) {
          interpreter.getContext().addGlobalMacro((MacroFunction) scopeEntry.getValue());
        } else if (scopeEntry.getKey().equals(Context.GLOBAL_MACROS_SCOPE_KEY)) {
          interpreter
            .getContext()
            .put(
              Context.GLOBAL_MACROS_SCOPE_KEY,
              new HashMap<>((Map<String, MacroFunction>) scopeEntry.getValue())
            );
        } else {
          if (!alreadyDeferredInEarlierCall(scopeEntry.getKey(), interpreter)) {
            interpreter.getContext().put(scopeEntry.getKey(), scopeEntry.getValue());
          }
        }
      }
    }

    // named parameters
    for (Map.Entry<String, Object> argEntry : argMap.entrySet()) {
      interpreter.getContext().put(argEntry.getKey(), argEntry.getValue());
    }
    // parameter map
    interpreter.getContext().put("kwargs", kwargMap);
    // varargs list
    interpreter.getContext().put("varargs", varArgs);

    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );
    try (
      TemporaryValueClosable<Boolean> c = interpreter.getContext().withUnwrapRawOverride()
    ) {
      for (Node node : content) {
        result.append(node.render(interpreter));
      }
    }
    return result.toString();
  }

  public void setDeferred(boolean deferred) {
    this.deferred = deferred;
  }

  public boolean isDeferred() {
    return deferred;
  }

  public boolean isCaller() {
    return caller;
  }

  public String reconstructImage() {
    if (content != null && !content.isEmpty()) {
      return content.get(0).getParent().reconstructImage();
    }
    return "";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MacroFunction that = (MacroFunction) o;
    return (
      caller == that.caller &&
      Objects.equals(getName(), that.getName()) &&
      Objects.equals(
        localContextScope.get(Context.IMPORT_RESOURCE_PATH_KEY),
        that.localContextScope.get(Context.IMPORT_RESOURCE_PATH_KEY)
      )
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      getName(),
      localContextScope.get(Context.IMPORT_RESOURCE_PATH_KEY),
      caller
    );
  }

  private boolean alreadyDeferredInEarlierCall(
    String key,
    JinjavaInterpreter interpreter
  ) {
    if (interpreter.getContext().get(key) instanceof DeferredValue) {
      Context penultimateParent = interpreter.getContext().getPenultimateParent();
      String importResourcePath = (String) localContextScope.get(
        Context.IMPORT_RESOURCE_PATH_KEY
      );
      return penultimateParent
        .getDeferredTokens()
        .stream()
        .filter(
          deferredToken ->
            Objects.equals(importResourcePath, deferredToken.getImportResourcePath())
        )
        .anyMatch(
          deferredToken ->
            deferredToken.getSetDeferredWords().contains(key) ||
            deferredToken
              .getUsedDeferredWords()
              .stream()
              .anyMatch(used -> key.equals(used.split("\\.", 2)[0]))
        );
    }
    return false;
  }
}
