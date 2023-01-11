package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import javax.el.ELContext;

public class EagerAstIdentifier extends AstIdentifier implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;

  public EagerAstIdentifier(String name, int index, boolean ignoreReturnType) {
    super(name, index, ignoreReturnType);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    return EvalResultHolder.super.eval(
      () -> {
        Object result = super.eval(bindings, context);
        if (
          !ExtendedParser.INTERPRETER.equals(getName()) &&
          !EagerExpressionResolver.isPrimitive(result) &&
          !(result instanceof Filter) &&
          (
            (JinjavaInterpreter) context
              .getELResolver()
              .getValue(context, null, ExtendedParser.INTERPRETER)
          ).getContext()
            .isPreserveAllIdentifiers()
        ) {
          throw new DeferredValueException("Preserving all non-primitive identifiers");
        }
        return result;
      },
      bindings,
      context
    );
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void setEvalResult(Object evalResult) {
    this.evalResult = evalResult;
    hasEvalResult = true;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    return getName();
  }
}
