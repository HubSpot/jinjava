package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstDot;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstDotDecorator extends AstDot implements EvalResultHolder {
  private Object evalResult;
  private final EvalResultHolder base;

  public EagerAstDotDecorator(
    AstNode base,
    String property,
    boolean lvalue,
    boolean ignoreReturnType
  ) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(base),
      property,
      lvalue,
      ignoreReturnType
    );
  }

  public EagerAstDotDecorator(
    EvalResultHolder base,
    String property,
    boolean lvalue,
    boolean ignoreReturnType
  ) {
    super((AstNode) base, property, lvalue, ignoreReturnType);
    this.base = base;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) throws ELException {
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        String.format("%s.%s", e.getDeferredEvalResult(), this.property)
      );
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
