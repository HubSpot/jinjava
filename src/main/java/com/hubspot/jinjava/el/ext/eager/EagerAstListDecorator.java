package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstList;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import javax.el.ELContext;

public class EagerAstListDecorator extends AstList implements EvalResultHolder {
  private Object evalResult;

  public EagerAstListDecorator(AstParameters elements) {
    super(elements);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    if (evalResult != null) {
      return evalResult;
    }
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
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
