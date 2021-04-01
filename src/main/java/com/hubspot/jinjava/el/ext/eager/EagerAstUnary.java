package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstUnary;
import javax.el.ELContext;

public class EagerAstUnary extends AstUnary implements EvalResultHolder {
  private Object evalResult;
  protected final EvalResultHolder child;
  protected final Operator operator;

  public EagerAstUnary(AstNode child, Operator operator) {
    this(EagerAstNodeDecorator.getAsEvalResultHolder(child), operator);
  }

  private EagerAstUnary(EvalResultHolder child, Operator operator) {
    super((AstNode) child, operator);
    this.child = child;
    this.operator = operator;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      String sb =
        operator.toString() +
        EvalResultHolder.reconstructNode(bindings, context, child, e, false);
      throw new DeferredParsingException(this, sb);
    } finally {
      child.getAndClearEvalResult();
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
