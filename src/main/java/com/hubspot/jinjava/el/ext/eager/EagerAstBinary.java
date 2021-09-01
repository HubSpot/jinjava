package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.NoInvokeELContext;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.OrOperator;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

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
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredParsingException e) {
      String sb =
        EvalResultHolder.reconstructNode(bindings, context, left, e, false) +
        String.format(" %s ", operator.toString()) +
        EvalResultHolder.reconstructNode(
          bindings,
          (operator instanceof OrOperator || operator == AstBinary.AND)
            ? new NoInvokeELContext(context) // short circuit on modification attempts because this may not be evaluated
            : context,
          right,
          e,
          false
        );
      throw new DeferredParsingException(this, sb);
    } finally {
      left.getAndClearEvalResult();
      right.getAndClearEvalResult();
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
