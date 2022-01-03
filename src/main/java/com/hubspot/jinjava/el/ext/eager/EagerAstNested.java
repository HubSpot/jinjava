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
    try {
      evalResult = child.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        this,
        String.format("(%s)", e.getDeferredEvalResult())
      );
    } finally {
      ((EvalResultHolder) child).getAndClearEvalResult();
    }
  }

  @Override
  public String toString() {
    return "(...)";
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

  @Override
  public int getCardinality() {
    return 1;
  }

  @Override
  public Node getChild(int i) {
    return i == 0 ? child : null;
  }
}
