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
public class EagerAstNestedDecorator extends AstRightValue implements EvalResultHolder {
  private Object evalResult;
  private final AstNode child;

  public EagerAstNestedDecorator(AstNode child) {
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
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = child.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        String.format("(%s)", e.getDeferredEvalResult())
      );
    }
  }

  @Override
  public String toString() {
    return "(...)";
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
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
