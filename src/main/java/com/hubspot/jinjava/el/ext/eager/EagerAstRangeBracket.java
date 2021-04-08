package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstRangeBracket;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstRangeBracket extends AstRangeBracket implements EvalResultHolder {
  protected Object evalResult;

  public EagerAstRangeBracket(
    AstNode base,
    AstNode rangeStart,
    AstNode rangeMax,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    super(
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(base),
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(rangeStart),
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(rangeMax),
      lvalue,
      strict,
      ignoreReturnType
    );
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      String sb =
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) prefix,
          e,
          false
        ) +
        "[" +
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) property,
          e,
          false
        ) +
        ":" +
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) rangeMax,
          e,
          false
        ) +
        "]";
      throw new DeferredParsingException(this, sb);
    } finally {
      if (prefix != null) {
        ((EvalResultHolder) prefix).getAndClearEvalResult();
      }
      if (property != null) {
        ((EvalResultHolder) property).getAndClearEvalResult();
      }
      if (rangeMax != null) {
        ((EvalResultHolder) rangeMax).getAndClearEvalResult();
      }
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
