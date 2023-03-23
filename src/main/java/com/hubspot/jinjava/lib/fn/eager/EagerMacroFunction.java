package com.hubspot.jinjava.lib.fn.eager;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.el.ext.AstMacroFunction;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.MacroTag;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@Beta
public class EagerMacroFunction extends AbstractCallableMethod {
  private String fullName;
  private MacroFunction macroFunction;
  private JinjavaInterpreter interpreter;

  public EagerMacroFunction(
    String fullName,
    MacroFunction macroFunction,
    JinjavaInterpreter interpreter
  ) {
    super(
      macroFunction.getName(),
      getLinkedHashmap(macroFunction.getArguments(), macroFunction.getDefaults())
    );
    this.fullName = fullName;
    this.macroFunction = macroFunction;
    this.interpreter = interpreter;
  }

  private static LinkedHashMap<String, Object> getLinkedHashmap(
    List<String> args,
    Map<String, Object> defaults
  ) {
    LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
    for (String arg : args) {
      linkedHashMap.put(arg, defaults.get(arg));
    }
    return linkedHashMap;
  }

  public Object doEvaluate(
    Map<String, Object> argMap,
    Map<String, Object> kwargMap,
    List<Object> varArgs
  ) {
    Optional<String> importFile = macroFunction.getImportFile(interpreter);
    try (InterpreterScopeClosable c = interpreter.enterNonStackingScope()) {
      interpreter.getContext().setDeferredExecutionMode(true);
      return macroFunction.getEvaluationResult(argMap, kwargMap, varArgs, interpreter);
    } finally {
      importFile.ifPresent(path -> interpreter.getContext().getCurrentPathStack().pop());
    }
  }

  public String getStartTag(JinjavaInterpreter interpreter) {
    StringJoiner argJoiner = new StringJoiner(", ");
    for (String arg : macroFunction.getArguments()) {
      if (macroFunction.getDefaults().get(arg) != null) {
        argJoiner.add(
          String.format(
            "%s=%s",
            arg,
            PyishObjectMapper.getAsPyishString(macroFunction.getDefaults().get(arg))
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

  public String getEndTag(JinjavaInterpreter interpreter) {
    return new StringJoiner(" ")
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(String.format("end%s", MacroTag.TAG_NAME))
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag())
      .toString();
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
  public String reconstructImage() {
    String prefix = "";
    String suffix = "";
    Optional<String> importFile = macroFunction.getImportFile(interpreter);
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
        interpreter.getContext().getMacroStack().contains(macroFunction.getName()) &&
        !differentMacroWithSameNameExists()
      ) ||
      (
        !macroFunction.isCaller() &&
        AstMacroFunction.checkAndPushMacroStack(interpreter, fullName)
      )
    ) {
      return "";
    } else {
      try (InterpreterScopeClosable c = interpreter.enterScope()) {
        String evaluation = (String) evaluate(
          macroFunction
            .getArguments()
            .stream()
            .map(arg -> DeferredMacroValueImpl.instance())
            .toArray()
        );

        if (!interpreter.getContext().getDeferredTokens().isEmpty()) {
          evaluation =
            (String) evaluate(
              macroFunction
                .getArguments()
                .stream()
                .map(arg -> DeferredMacroValueImpl.instance())
                .toArray()
            );
        }
        result = (getStartTag(interpreter) + evaluation + getEndTag(interpreter));
      } catch (DeferredValueException e) {
        // In case something not eager-supported encountered a deferred value
        result = macroFunction.reconstructImage();
      } finally {
        interpreter
          .getContext()
          .put(Context.DEFERRED_IMPORT_RESOURCE_PATH_KEY, currentDeferredImportResource);
        if (!macroFunction.isCaller()) {
          interpreter.getContext().getMacroStack().pop();
        }
      }
    }
    return prefix + result + suffix;
  }

  private boolean differentMacroWithSameNameExists() {
    Context context = interpreter.getContext();
    if (context.getParent() == null) {
      return false;
    }
    MacroFunction mostRecent = context.getGlobalMacro(macroFunction.getName());
    if (macroFunction != mostRecent) {
      return true;
    }
    while (
      !context.getGlobalMacros().containsKey(macroFunction.getName()) &&
      context.getParent().getParent() != null
    ) {
      context = context.getParent();
    }
    MacroFunction secondMostRecent = context
      .getParent()
      .getGlobalMacro(macroFunction.getName());
    return secondMostRecent != null && secondMostRecent != macroFunction;
  }
}
