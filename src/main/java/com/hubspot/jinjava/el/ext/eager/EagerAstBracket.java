package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBracket;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstBracket extends AstBracket implements EvalResultHolder {
  protected Object evalResult;

  public EagerAstBracket(
    AstNode base,
    AstNode property,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    super(
      (AstNode) EagerAstNode.getAsEvalResultHolder(base),
      (AstNode) EagerAstNode.getAsEvalResultHolder(property),
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
      StringBuilder sb = new StringBuilder();
      if (((EvalResultHolder) prefix).hasEvalResult()) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) prefix).getAndClearEvalResult()
          )
        );
        sb.append(String.format("[%s]", e.getDeferredEvalResult()));
      } else {
        sb.append(e.getDeferredEvalResult());
        try {
          sb.append(
            String.format(
              "[%s]",
              ChunkResolver.getValueAsJinjavaStringSafe(property.eval(bindings, context))
            )
          );
        } catch (DeferredParsingException e1) {
          sb.append(String.format("[%s]", e1.getDeferredEvalResult()));
        }
      }
      throw new DeferredParsingException(sb.toString());
    } finally {
      ((EvalResultHolder) prefix).getAndClearEvalResult();
      ((EvalResultHolder) property).getAndClearEvalResult();
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
