package com.hubspot.jinjava.lib.fn.eager;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.MacroTag;
import com.hubspot.jinjava.lib.tag.eager.EagerTagDecorator;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

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
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      interpreter.getContext().setDeferredExecutionMode(true);
      return macroFunction.getEvaluationResult(argMap, kwargMap, varArgs, interpreter);
    } finally {
      importFile.ifPresent(path -> interpreter.getContext().getCurrentPathStack().pop());
    }
  }

  public String getStartTag(JinjavaInterpreter interpreter) {
    StringJoiner argJoiner = new StringJoiner(", ");
    PyishObjectMapper pyishObjectMapper = interpreter.getContext().getPyishObjectMapper();
    for (String arg : macroFunction.getArguments()) {
      if (macroFunction.getDefaults().get(arg) != null) {
        argJoiner.add(
          String.format(
            "%s=%s",
            arg,
            pyishObjectMapper.getAsPyishString(macroFunction.getDefaults().get(arg))
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
    if (importFile.isPresent()) {
      interpreter.getContext().getCurrentPathStack().pop();
      if (fullName.indexOf('.') >= 0) {
        prefix = getAliasedImportResourcePrefix(importFile.get());
      } else {
        prefix =
          EagerTagDecorator.buildSetTagForDeferredInChildContext(
            ImmutableMap.of(
              Context.IMPORT_RESOURCE_PATH_KEY,
              interpreter
                .getContext()
                .getPyishObjectMapper()
                .getAsPyishString(importFile.get())
            ),
            interpreter,
            false
          );
        String currentImportResource = interpreter
          .getContext()
          .getCurrentPathStack()
          .peek()
          .orElse("");
        suffix =
          EagerTagDecorator.buildSetTagForDeferredInChildContext(
            ImmutableMap.of(
              Context.IMPORT_RESOURCE_PATH_KEY,
              interpreter
                .getContext()
                .getPyishObjectMapper()
                .getAsPyishString(currentImportResource)
            ),
            interpreter,
            false
          );
      }
    }

    String result;
    try {
      String evaluation = (String) evaluate(
        macroFunction
          .getArguments()
          .stream()
          .map(arg -> DeferredValue.instance())
          .toArray()
      );
      result = (getStartTag(interpreter) + evaluation + getEndTag(interpreter));
    } catch (DeferredValueException e) {
      // In case something not eager-supported encountered a deferred value
      result = macroFunction.reconstructImage();
    }
    return prefix + result + suffix;
  }

  private String getAliasedImportResourcePrefix(String importResourcePath) {
    String prefix = "";
    String importAlias = fullName.split("\\.", 2)[0];
    Object aliasMap = interpreter.getContext().get(importAlias);
    if (aliasMap instanceof DeferredValue) {
      PyMap deferredAliasMap = new PyMap(new HashMap<>());
      deferredAliasMap.put(Context.IMPORT_RESOURCE_PATH_KEY, importResourcePath);
      prefix =
        EagerTagDecorator.buildDoUpdateTag(
          importAlias,
          interpreter
            .getContext()
            .getPyishObjectMapper()
            .getAsPyishString(deferredAliasMap),
          interpreter
        );
    } else if (aliasMap instanceof Map) {
      PyMap deferredAliasMap = new PyMap(new HashMap<>());
      deferredAliasMap.put(Context.IMPORT_RESOURCE_PATH_KEY, importResourcePath);
      prefix =
        EagerTagDecorator.buildSetTagForDeferredInChildContext(
          ImmutableMap.of(
            importAlias,
            interpreter
              .getContext()
              .getPyishObjectMapper()
              .getAsPyishString(deferredAliasMap)
          ),
          interpreter,
          false
        );
    }
    return prefix;
  }
}
