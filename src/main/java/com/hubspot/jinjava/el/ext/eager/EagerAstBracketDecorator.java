package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBracket;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstBracketDecorator extends AstBracket implements EvalResultHolder {
  protected Object evalResult;

  public EagerAstBracketDecorator(
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
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = super.eval(bindings, context);
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      if (((EvalResultHolder) prefix).getEvalResult() != null) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) prefix).getEvalResult()
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
    }
    return evalResult;
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
