package com.hubspot.jinjava.el.ext.eager;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import javax.el.ELContext;

public class EagerAstIdentifier extends AstIdentifier implements EvalResultHolder {
  private Object evalResult;

  public EagerAstIdentifier(String name, int index, boolean ignoreReturnType) {
    super(name, index, ignoreReturnType);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    evalResult = super.eval(bindings, context);
    return evalResult;
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
