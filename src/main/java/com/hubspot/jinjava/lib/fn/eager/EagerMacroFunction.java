package com.hubspot.jinjava.lib.fn.eager;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.ext.AstMacroFunction;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.eager.MacroFunctionTempVariable;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.MacroTag;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.lib.tag.eager.EagerExecutionResult;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

public class EagerMacroFunction extends MacroFunction {
  private AtomicInteger callCount = new AtomicInteger();
  private boolean reconstructing = false;

  public EagerMacroFunction(
    List<Node> content,
    String name,
    LinkedHashMap<String, Object> argNamesWithDefaults,
    boolean caller,
    Context localContextScope,
    int lineNumber,
    int startPosition
  ) {
    super(
      content,
      name,
      argNamesWithDefaults,
      caller,
      localContextScope,
      lineNumber,
      startPosition
    );
  }

  public Object doEvaluate(
    Map<String, Object> argMap,
    Map<String, Object> kwargMap,
    List<Object> varArgs
  ) {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    if (reconstructing) {
      Optional<String> importFile = getImportFile(interpreter);
      try (InterpreterScopeClosable c = interpreter.enterNonStackingScope()) {
        interpreter.getContext().setDeferredExecutionMode(true);
        return getEvaluationResult(argMap, kwargMap, varArgs, interpreter);
      } finally {
        importFile.ifPresent(
          path -> interpreter.getContext().getCurrentPathStack().pop()
        );
      }
    }

    int currentCallCount = callCount.getAndIncrement();
    Set<DeferredToken> addedTokens = new HashSet<>();
    EagerExecutionResult result = EagerContextWatcher.executeInChildContext(
      eagerInterpreter -> {
        EagerExpressionResult expressionResult = EagerExpressionResult.fromSupplier(
          () -> super.doEvaluate(argMap, kwargMap, varArgs).toString(),
          eagerInterpreter
        );
        addedTokens.addAll(eagerInterpreter.getContext().getDeferredTokens());
        return expressionResult;
      },
      interpreter,
      EagerContextWatcher
        .EagerChildContextConfig.newBuilder()
        .withCheckForContextChanges(!interpreter.getContext().isDeferredExecutionMode())
        .build()
    );
    if (
      !result.getResult().isFullyResolved() &&
      (
        !interpreter.getContext().isPartialMacroEvaluation() ||
        !result.getSpeculativeBindings().isEmpty() ||
        interpreter.getContext().isDeferredExecutionMode()
      )
    ) {
      //        EagerReconstructionUtils.resetSpeculativeBindings(interpreter, result);
      //        interpreter.getContext().removeDeferredTokens(addedTokens);
      EagerExecutionResult firstRunResult = runLoopOnce(
        argMap,
        kwargMap,
        varArgs,
        interpreter
      );
      String tempVarName = MacroFunctionTempVariable.getVarName(
        getName(),
        hashCode(),
        currentCallCount
      );
      interpreter
        .getContext()
        .getParent()
        .put(
          tempVarName,
          new MacroFunctionTempVariable(firstRunResult.asTemplateString())
        );
      throw new DeferredParsingException(this, tempVarName);
      //      }
      //
      //      if (
      //        interpreter.getContext().isDeferredExecutionMode() ||
      //        !result.getSpeculativeBindings().isEmpty()
      //      ) {
      //        return reconstructImage();
      //        EagerReconstructionUtils.resetSpeculativeBindings(interpreter, result);
      //        interpreter.getContext().removeDeferredTokens(addedTokens);
      //        String prefix = "";
      //
      //        EagerExecutionResult firstRunResult = runLoopOnce(
      //          argMap,
      //          kwargMap,
      //          varArgs,
      //          interpreter
      //        );
      //        if (!firstRunResult.getSpeculativeBindings().isEmpty()) {
      //          //          Set<String> toRemove = firstRunResult
      //          //            .getSpeculativeBindings()
      //          //            .keySet()
      //          //            .stream()
      //          //            .filter(key -> argMap.containsKey(key) || kwargMap.containsKey(key))
      //          //            .collect(Collectors.toSet());
      //          //          toRemove.forEach(key -> firstRunResult.getSpeculativeBindings().remove(key));
      //          // Defer any variables that we tried to modify during the loop
      //          prefix = firstRunResult.getPrefixToPreserveState(true);
      //        }
      //        // Run for loop again now that the necessary values have been deferred
      //        EagerExecutionResult secondRunResult = runLoopOnce(
      //          argMap,
      //          kwargMap,
      //          varArgs,
      //          interpreter
      //        );
      //        if (
      //          secondRunResult
      //            .getSpeculativeBindings()
      //            .keySet()
      //            .stream()
      //            .anyMatch(key -> !firstRunResult.getSpeculativeBindings().containsKey(key))
      //        ) {
      //          throw new DeferredValueException(
      //            "Modified values in deferred for loop: " +
      //            String.join(", ", secondRunResult.getSpeculativeBindings().keySet())
      //          );
      //        }
      //        return (
      //          prefix +
      //          EagerReconstructionUtils.wrapInChildScope(
      //            secondRunResult.getResult().toString(true),
      //            interpreter
      //          )
      //        );
      //      }
    }
    return result.getResult().toString(true);
  }

  private EagerExecutionResult runLoopOnce(
    Map<String, Object> argMap,
    Map<String, Object> kwargMap,
    List<Object> varArgs,
    JinjavaInterpreter interpreter
  ) {
    return EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResult.fromSupplier(
          () -> super.doEvaluate(argMap, kwargMap, varArgs).toString(),
          eagerInterpreter
        ),
      interpreter,
      EagerContextWatcher
        .EagerChildContextConfig.newBuilder()
        .withForceDeferredExecutionMode(true)
        .build()
    );
  }

  private String getStartTag(String fullName, JinjavaInterpreter interpreter) {
    StringJoiner argJoiner = new StringJoiner(", ");
    for (String arg : getArguments()) {
      if (getDefaults().get(arg) != null) {
        argJoiner.add(
          String.format(
            "%s=%s",
            arg,
            PyishObjectMapper.getAsPyishString(getDefaults().get(arg))
          )
        );
        continue;
      }
      argJoiner.add(arg);
    }
    return new StringJoiner(" ")
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(MacroTag.TAG_NAME)
      .add(String.format("%s(%s)", fullName, argJoiner.toString()))
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag())
      .toString();
  }

  private String getEndTag(JinjavaInterpreter interpreter) {
    return new StringJoiner(" ")
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(String.format("end%s", MacroTag.TAG_NAME))
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag())
      .toString();
  }

  @Override
  public String reconstructImage() {
    return reconstructImage(getName());
  }

  /**
   * Reconstruct the image of the macro function, @see MacroFunction#reconstructImage()
   * This image, however, may be partially or fully resolved depending on the
   * usage of the arguments, which are filled in as deferred values, and any values on
   * this interpreter's context.
   * @return An image of the macro function that's body is resolved as much as possible.
   *  This image allows for the macro function to be recreated during a later
   *  rendering pass.
   */
  public String reconstructImage(String fullName) {
    String prefix = "";
    String suffix = "";
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    Optional<String> importFile = getImportFile(interpreter);
    Object currentDeferredImportResource = null;
    if (importFile.isPresent()) {
      interpreter.getContext().getCurrentPathStack().pop();
      currentDeferredImportResource =
        interpreter.getContext().get(Context.DEFERRED_IMPORT_RESOURCE_PATH_KEY);
      if (currentDeferredImportResource instanceof DeferredValue) {
        currentDeferredImportResource =
          ((DeferredValue) currentDeferredImportResource).getOriginalValue();
      }
      prefix =
        EagerReconstructionUtils.buildSetTag(
          ImmutableMap.of(
            Context.DEFERRED_IMPORT_RESOURCE_PATH_KEY,
            PyishObjectMapper.getAsPyishString(importFile.get())
          ),
          interpreter,
          false
        );
      interpreter
        .getContext()
        .put(Context.DEFERRED_IMPORT_RESOURCE_PATH_KEY, importFile.get());
      suffix =
        EagerReconstructionUtils.buildSetTag(
          ImmutableMap.of(
            Context.DEFERRED_IMPORT_RESOURCE_PATH_KEY,
            PyishObjectMapper.getAsPyishString(currentDeferredImportResource)
          ),
          interpreter,
          false
        );
    }

    String result;
    if (
      (
        interpreter.getContext().getMacroStack().contains(getName()) &&
        !differentMacroWithSameNameExists(interpreter)
      ) ||
      (!isCaller() && AstMacroFunction.checkAndPushMacroStack(interpreter, fullName))
    ) {
      return "";
    } else {
      try (InterpreterScopeClosable c = interpreter.enterScope()) {
        reconstructing = true;
        String evaluation = (String) evaluate(
          getArguments().stream().map(arg -> DeferredMacroValueImpl.instance()).toArray()
        );

        if (!interpreter.getContext().getDeferredTokens().isEmpty()) {
          evaluation =
            (String) evaluate(
              getArguments()
                .stream()
                .map(arg -> DeferredMacroValueImpl.instance())
                .toArray()
            );
        }
        result =
          (getStartTag(fullName, interpreter) + evaluation + getEndTag(interpreter));
      } catch (DeferredValueException e) {
        // In case something not eager-supported encountered a deferred value
        result = super.reconstructImage();
      } finally {
        reconstructing = false;
        interpreter
          .getContext()
          .put(Context.DEFERRED_IMPORT_RESOURCE_PATH_KEY, currentDeferredImportResource);
        if (!isCaller()) {
          interpreter.getContext().getMacroStack().pop();
        }
      }
    }
    return prefix + result + suffix;
  }

  private boolean differentMacroWithSameNameExists(JinjavaInterpreter interpreter) {
    Context context = interpreter.getContext();
    if (context.getParent() == null) {
      return false;
    }
    MacroFunction mostRecent = context.getGlobalMacro(getName());
    if (this != mostRecent) {
      return true;
    }
    while (
      !context.getGlobalMacros().containsKey(getName()) &&
      context.getParent().getParent() != null
    ) {
      context = context.getParent();
    }
    MacroFunction secondMostRecent = context.getParent().getGlobalMacro(getName());
    return secondMostRecent != null && secondMostRecent != this;
  }
}
