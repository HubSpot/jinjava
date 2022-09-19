package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.NoInvokeELContext;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.OrOperator;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import jakarta.el.ELContext;

public class EagerAstBinary extends AstBinary implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;
  protected final EvalResultHolder left;
  protected final EvalResultHolder right;
  protected final Operator operator;

  public EagerAstBinary(AstNode left, AstNode right, Operator operator) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(left),
      EagerAstNodeDecorator.getAsEvalResultHolder(right),
      operator
    );
  }

  private EagerAstBinary(
    EvalResultHolder left,
    EvalResultHolder right,
    Operator operator
  ) {
    super((AstNode) left, (AstNode) right, operator);
    this.left = left;
    this.right = right;
    this.operator = operator;
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
    return (
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        left,
        deferredParsingException,
        false
      ) +
      String.format(" %s ", operator.toString()) +
      EvalResultHolder.reconstructNode(
        bindings,
        (operator instanceof OrOperator || operator == AstBinary.AND)
          ? new NoInvokeELContext(context) // short circuit on modification attempts because this may not be evaluated
          : context,
        right,
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
