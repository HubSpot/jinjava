package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBracket;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstBracketDecorator extends AstBracket implements EvalResultHolder {
  private Object evalResult;
  private final EvalResultHolder base;
  private final EvalResultHolder property;
  private final boolean lvalue;

  public EagerAstBracketDecorator(
    AstNode base,
    AstNode property,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(base),
      EagerAstNodeDecorator.getAsEvalResultHolder(property),
      lvalue,
      strict,
      ignoreReturnType
    );
  }

  private EagerAstBracketDecorator(
    EvalResultHolder base,
    EvalResultHolder property,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    super((AstNode) base, (AstNode) property, lvalue, strict, ignoreReturnType);
    this.base = base;
    this.property = property;
    this.lvalue = lvalue;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = super.eval(bindings, context);
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      if (base.getEvalResult() != null) {
        sb.append(base.getEvalResult());
        sb.append(String.format("[%s]", e.getDeferredEvalResult()));
      } else {
        sb.append(e.getDeferredEvalResult());
        try {
          sb.append(String.format("[%s]", ((AstNode) property).eval(bindings, context)));
        } catch (DeferredParsingException e1) {
          sb.append(String.format("[%s]", e1.getDeferredEvalResult()));
        }
      }
      throw new DeferredParsingException(sb.toString());
    }
    return evalResult;
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
