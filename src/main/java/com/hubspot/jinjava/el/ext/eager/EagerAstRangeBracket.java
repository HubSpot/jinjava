package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstRangeBracket;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;

public class EagerAstRangeBracket extends AstRangeBracket implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;

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
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
      String sb =
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) prefix,
          e,
          true
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
    hasEvalResult = false;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
