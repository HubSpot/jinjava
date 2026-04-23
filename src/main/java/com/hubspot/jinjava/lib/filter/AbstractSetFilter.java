package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.features.BuiltInFeatures;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.TypeFunction;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractSetFilter implements AdvancedFilter {

  protected Object parseArgs(JinjavaInterpreter interpreter, Object[] args) {
    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (a list to perform set function)"
      );
    }

    return args[0];
  }

  protected Set<Object> objectToSet(Object var) {
    Set<Object> result = new LinkedHashSet<>();
    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      result.add(loop.next());
    }
    return result;
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    Set<Object> varSet = objectToSet(var);
    Set<Object> argSet = objectToSet(parseArgs(interpreter, args));

    if (!varSet.isEmpty() && !argSet.isEmpty()) {
      Object oneVar = varSet.iterator().next();
      Object oneArg = argSet.iterator().next();

      boolean featureActive = interpreter
        .getConfig()
        .getFeatures()
        .isActive(
          BuiltInFeatures.INTEGER_SET_TO_LONG_CONVERSION,
          interpreter.getContext()
        );
      if (featureActive) {
        if (oneVar instanceof Integer && oneArg instanceof Long) {
          varSet = convertIntegersToLongs(varSet);
        } else if (oneArg instanceof Integer && oneVar instanceof Long) {
          argSet = convertIntegersToLongs(argSet);
        }
      }

      attachMismatchedTypesWarning(interpreter, varSet, argSet, oneVar, oneArg);
    }

    return filter(varSet, argSet);
  }

  public abstract Object filter(Set<Object> varSet, Set<Object> argSet);

  protected void attachMismatchedTypesWarning(
    JinjavaInterpreter interpreter,
    Set<Object> varSet,
    Set<Object> argSet
  ) {
    if (varSet.isEmpty() || argSet.isEmpty()) {
      return;
    }
    attachMismatchedTypesWarning(
      interpreter,
      varSet,
      argSet,
      varSet.iterator().next(),
      argSet.iterator().next()
    );
  }

  private void attachMismatchedTypesWarning(
    JinjavaInterpreter interpreter,
    Set<Object> varSet,
    Set<Object> argSet,
    Object oneVarObj,
    Object oneArgObj
  ) {
    if (getTypeOfSetElements(varSet).equals(getTypeOfSetElements(argSet))) {
      return;
    }
    if (potentiallyConvertibleNumbers(oneVarObj, oneArgObj)) {
      return;
    }
    interpreter.addError(
      new TemplateError(
        TemplateError.ErrorType.WARNING,
        TemplateError.ErrorReason.OTHER,
        TemplateError.ErrorItem.FILTER,
        String.format(
          "Mismatched Types: input set has elements of type '%s' but arg set has elements of type '%s'. Use |map filter to convert sets to the same type for filter to work correctly.",
          getTypeOfSetElements(varSet),
          getTypeOfSetElements(argSet)
        ),
        "list",
        interpreter.getLineNumber(),
        interpreter.getPosition(),
        null
      )
    );
  }

  private boolean potentiallyConvertibleNumbers(Object oneVarObj, Object oneArgObj) {
    return (
      (oneArgObj instanceof Integer && oneVarObj instanceof Long) ||
      (oneVarObj instanceof Integer && oneArgObj instanceof Long)
    );
  }

  private Set<Object> convertIntegersToLongs(Set<Object> set) {
    Set<Object> result = new LinkedHashSet<>();
    for (Object element : set) {
      if (element instanceof Integer integer) {
        result.add(integer.longValue());
      } else {
        result.add(element);
      }
    }
    return result;
  }

  private String getTypeOfSetElements(Set<Object> set) {
    return TypeFunction.type(set.iterator().next());
  }
}
