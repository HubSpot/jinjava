package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.TypeFunction;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import java.util.LinkedHashSet;
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

  protected void attachMismatchedTypesWarning(
    JinjavaInterpreter interpreter,
    Set<Object> varSet,
    Set<Object> argSet
  ) {
    boolean hasAtLeastOneSetEmpty = varSet.isEmpty() || argSet.isEmpty();
    if (hasAtLeastOneSetEmpty) {
      return;
    }

    boolean areMatchedElementTypes = getTypeOfSetElements(varSet)
      .equals(getTypeOfSetElements(argSet));
    if (areMatchedElementTypes) {
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
        -1,
        null
      )
    );
  }

  private String getTypeOfSetElements(Set<Object> set) {
    if (set.isEmpty()) {
      return "null";
    }
    return TypeFunction.type(set.iterator().next());
  }
}
