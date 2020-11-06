package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstTuple;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import javax.el.ELContext;

public class EagerAstTupleDecorator extends AstTuple implements EvalResultHolder {
  private Object evalResult;

  public EagerAstTupleDecorator(AstParameters elements) {
    super(elements);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      try {
        elements.eval(bindings, context);
      } catch (DeferredParsingException e1) {
        throw new DeferredParsingException(
          String.format("[%s]", e1.getDeferredEvalResult())
        );
      }
      throw new DeferredParsingException(
        String.format("[%s]", e.getDeferredEvalResult())
      );
    } finally {
      ((EvalResultHolder) elements).getAndClearEvalResult();
    }
  }

  @Override
  public Object getAndClearEvalResult() {
    Object temp = evalResult;
    evalResult = null;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return evalResult != null;
  }
}
