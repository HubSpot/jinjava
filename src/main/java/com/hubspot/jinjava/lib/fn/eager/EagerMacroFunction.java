package com.hubspot.jinjava.lib.fn.eager;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.MacroTag;
import com.hubspot.jinjava.objects.PyishObjectMapper;
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
    PyishObjectMapper pyishObjectMapper = interpreter.getContext().getPyishClassMapper();
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
    String result;
    try {
      result =
        (String) evaluate(
          macroFunction
            .getArguments()
            .stream()
            .map(arg -> DeferredValue.instance())
            .toArray()
        );
    } catch (DeferredValueException e) {
      // In case something not eager-supported encountered a deferred value
      return macroFunction.reconstructImage();
    }

    return (getStartTag(interpreter) + result + getEndTag(interpreter));
  }
}
