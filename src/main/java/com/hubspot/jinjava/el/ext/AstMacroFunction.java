package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
import com.hubspot.jinjava.interpret.CallStack;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.MacroTagCycleException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import de.odysseus.el.misc.LocalMessages;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstFunction;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.lang.reflect.InvocationTargetException;
import javax.el.ELContext;
import javax.el.ELException;

public class AstMacroFunction extends AstFunction {

  public AstMacroFunction(String name, int index, AstParameters params, boolean varargs) {
    super(name, index, params, varargs);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    JinjavaInterpreter interpreter = (JinjavaInterpreter) context
      .getELResolver()
      .getValue(context, null, ExtendedParser.INTERPRETER);

    MacroFunction macroFunction = interpreter.getContext().getGlobalMacro(getName());
    if (macroFunction != null) {
      if (macroFunction.isDeferred()) {
        throw new DeferredValueException(
          getName(),
          interpreter.getLineNumber(),
          interpreter.getPosition()
        );
      }
      if (macroFunction.isCaller()) {
        return wrapInvoke(bindings, context, macroFunction);
      }
      try (
        AutoCloseableImpl<Boolean> macroStackPush = checkAndPushMacroStackWithWrapper(
          interpreter,
          getName()
        )
          .get()
      ) {
        if (macroStackPush.value()) {
          return "";
        }

        return wrapInvoke(bindings, context, macroFunction);
      }
    }

    return interpreter.getContext().isValidationMode()
      ? ""
      : super.eval(bindings, context);
  }

  private Object wrapInvoke(
    Bindings bindings,
    ELContext context,
    MacroFunction macroFunction
  ) {
    try {
      return invoke(bindings, context, macroFunction, AbstractCallableMethod.EVAL_METHOD);
    } catch (IllegalAccessException e) {
      throw new ELException(LocalMessages.get("error.function.access", getName()), e);
    } catch (InvocationTargetException e) {
      throw new ELException(
        LocalMessages.get("error.function.invocation", getName()),
        e.getCause()
      );
    }
  }

  public static AutoCloseableSupplier<Boolean> checkAndPushMacroStackWithWrapper(
    JinjavaInterpreter interpreter,
    String name
  ) {
    CallStack macroStack = interpreter.getContext().getMacroStack();
    try {
      if (interpreter.getConfig().isEnableRecursiveMacroCalls()) {
        if (interpreter.getConfig().getMaxMacroRecursionDepth() != 0) {
          return macroStack
            .closeablePushWithMaxDepth(
              name,
              interpreter.getConfig().getMaxMacroRecursionDepth(),
              interpreter.getLineNumber(),
              interpreter.getPosition()
            )
            .map(macro -> false);
        } else {
          return macroStack
            .closeablePushWithoutCycleCheck(
              name,
              interpreter.getLineNumber(),
              interpreter.getPosition()
            )
            .map(macro -> false);
        }
      }
      return macroStack.closeablePush(name, -1, -1).map(macro -> false);
    } catch (MacroTagCycleException e) {
      int maxDepth = interpreter.getConfig().getMaxMacroRecursionDepth();
      if (maxDepth != 0 && interpreter.getConfig().isValidationMode()) {
        // validation mode is only concerned with syntax
        return AutoCloseableSupplier.of(true);
      }

      String message = maxDepth == 0
        ? String.format("Cycle detected for macro '%s'", name)
        : String.format(
          "Max recursion limit of %d reached for macro '%s'",
          maxDepth,
          name
        );

      interpreter.addError(
        new TemplateError(
          TemplateError.ErrorType.WARNING,
          TemplateError.ErrorReason.EXCEPTION,
          TemplateError.ErrorItem.TAG,
          message,
          null,
          e.getLineNumber(),
          e.getStartPosition(),
          e,
          BasicTemplateErrorCategory.CYCLE_DETECTED,
          ImmutableMap.of("name", name)
        )
      );

      return AutoCloseableSupplier.of(true);
    }
  }

  @Deprecated
  public static boolean checkAndPushMacroStack(
    JinjavaInterpreter interpreter,
    String name
  ) {
    return checkAndPushMacroStackWithWrapper(interpreter, name)
      .dangerouslyGetWithoutClosing();
  }
}
