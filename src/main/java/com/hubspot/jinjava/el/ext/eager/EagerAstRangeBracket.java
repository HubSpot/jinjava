package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstRangeBracket;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
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
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      if (((EvalResultHolder) prefix).getEvalResult() != null) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) prefix).getEvalResult()
          )
        );
      } else {
        sb.append(e.getDeferredEvalResult());
        e = null;
      }
      sb.append("[");
      if (((EvalResultHolder) property).getEvalResult() != null) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) property).getEvalResult()
          )
        );
      } else if (e != null) {
        sb.append(e.getDeferredEvalResult());
        e = null;
      } else {
        try {
          sb.append(
            ChunkResolver.getValueAsJinjavaStringSafe(property.eval(bindings, context))
          );
        } catch (DeferredParsingException e1) {
          sb.append(e1.getDeferredEvalResult());
        }
      }
      sb.append(":");
      if (((EvalResultHolder) rangeMax).getEvalResult() != null) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) rangeMax).getEvalResult()
          )
        );
      } else if (e != null) {
        sb.append(e.getDeferredEvalResult());
      } else {
        try {
          sb.append(
            ChunkResolver.getValueAsJinjavaStringSafe(rangeMax.eval(bindings, context))
          );
        } catch (DeferredParsingException e1) {
          sb.append(e1.getDeferredEvalResult());
        }
      }
      sb.append("]");
      throw new DeferredParsingException(sb.toString());
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
