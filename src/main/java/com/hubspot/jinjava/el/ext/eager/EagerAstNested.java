package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.Node;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import com.hubspot.jinjava.el.tree.impl.ast.AstRightValue;
import jakarta.el.ELContext;

/**
 * AstNested is final so this decorates AstRightValue.
 */
public class EagerAstNested extends AstRightValue implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;
  protected final AstNode child;

  public EagerAstNested(AstNode child) {
    this.child = child;
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    builder.append("(");
    child.appendStructure(builder, bindings);
    builder.append(")");
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    return EvalResultHolder.super.eval(
      () -> child.eval(bindings, context),
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
  public String toString() {
    return "(...)";
  }

  @Override
  public void clearEvalResult() {
    evalResult = null;
    hasEvalResult = false;
    ((EvalResultHolder) child).clearEvalResult();
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }

  @Override
  public int getCardinality() {
    return 1;
  }

  @Override
  public Node getChild(int i) {
    return i == 0 ? child : null;
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    return String.format(
      "(%s)",
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        (EvalResultHolder) child,
        deferredParsingException,
        preserveIdentifier
      )
    );
  }
}
