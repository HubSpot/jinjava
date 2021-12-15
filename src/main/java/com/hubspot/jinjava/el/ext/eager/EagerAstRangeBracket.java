package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstRangeBracket;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.ELException;

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
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void clearEvalResult() {
    evalResult = null;
    hasEvalResult = false;
    if (prefix != null) {
      ((EvalResultHolder) prefix).clearEvalResult();
    }
    if (property != null) {
      ((EvalResultHolder) property).clearEvalResult();
    }
    if (rangeMax != null) {
      ((EvalResultHolder) rangeMax).clearEvalResult();
    }
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
