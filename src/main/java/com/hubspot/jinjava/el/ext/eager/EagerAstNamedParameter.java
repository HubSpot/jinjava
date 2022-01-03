package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstNamedParameter;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstIdentifier;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import jakarta.el.ELContext;

public class EagerAstNamedParameter
  extends AstNamedParameter
  implements EvalResultHolder {
  protected boolean hasEvalResult;
  protected Object evalResult;
  protected final AstIdentifier name;
  protected final EvalResultHolder value;

  public EagerAstNamedParameter(AstIdentifier name, AstNode value) {
    this(name, EagerAstNodeDecorator.getAsEvalResultHolder(value));
  }

  private EagerAstNamedParameter(AstIdentifier name, EvalResultHolder value) {
    super(name, (AstNode) value);
    this.name = name;
    this.value = value;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        this,
        String.format("%s=%s", name, e.getDeferredEvalResult())
      );
    } finally {
      value.getAndClearEvalResult();
    }
  }

  @Override
  public Object getAndClearEvalResult() {
    Object temp = evalResult;
    evalResult = null;
    hasEvalResult = false;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
