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
    return EvalResultHolder.super.eval(
      () -> super.eval(bindings, context),
      bindings,
      context
    );
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    return String.format(
      "%s=%s",
      name,
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        value,
        deferredParsingException,
        false
      )
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
}
