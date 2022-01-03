package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstBracket;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;

public class EagerAstBracket extends AstBracket implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;

  public EagerAstBracket(
    AstNode base,
    AstNode property,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    super(
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(base),
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(property),
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
      String sb = String.format(
        "%s[%s]",
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) prefix,
          e,
          true
        ),
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) property,
          e,
          false
        )
      );
      throw new DeferredParsingException(this, sb);
    } finally {
      ((EvalResultHolder) prefix).getAndClearEvalResult();
      ((EvalResultHolder) property).getAndClearEvalResult();
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

  public AstNode getPrefix() {
    return prefix;
  }

  public AstNode getMethod() {
    return property;
  }
}
