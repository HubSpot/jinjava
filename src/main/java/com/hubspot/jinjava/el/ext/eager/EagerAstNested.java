package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.Node;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstRightValue;
import javax.el.ELContext;

/**
 * AstNested is final so this decorates AstRightValue.
 */
public class EagerAstNested extends AstRightValue implements EvalResultHolder {
  private Object evalResult;
  private final AstNode child;

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
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return evalResult != null;
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
